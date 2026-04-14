# Phase 2 — TTL Groundwork (Detailed)

> Timebox: Dec 2025 (~4 weeks @ 10–15h/week)
> Target outcome: expiry-aware data model with **lazy** deletion on access; command stubs wired; metrics to observe TTL behavior. No background expiry yet.

## Scope (what ships in Phase 2)

* Add expiry metadata to stored entries (`expireAtMillis`, 0 = no TTL).
* Enforce **lazy expiry** uniformly on reads and key-existence checks.
* Command surface: wire `EXPIRE`, `PEXPIRE`, `TTL`, `PTTL`, `PERSIST`.

    * Stubs are acceptable for week 1; by end of phase: functional semantics for all five.
* Metrics: `ttl_lazy_expired_evictions` (counter). Optionally `ttl_keys_with_expiry` (gauge).
* Extend `INFO` to include TTL stats (no schema churn for existing fields).

## Non-Goals (defer)

* Background/active expiry sweeps.
* Memory cap and eviction policies.
* Precision histograms or percentile latencies.

## Milestones

1. M1 — Data model change + lazy expiry path in GET/EXISTS (week 1)
2. M2 — `EXPIRE/PEXPIRE` functional; `PERSIST` clears expiry (week 2)
3. M3 — `TTL/PTTL` functional + metric counters/gauges + INFO section (week 3)
4. M4 — Semantics polish, edge cases, naming freeze for TTL metrics (week 4)

## Work Breakdown (sequence)

1. **Data model extension**
   Add `expireAtMillis` to the stored value record/entry. Convention: `0` = no expiry.
   Reads compare `now = System.currentTimeMillis()`; if `now >= expireAtMillis` → delete + count lazy eviction.

2. **Uniform lazy expiry hooks**
   Apply check-and-delete in all read-like paths: `GET`, `EXISTS`, `GETSET` (old value fetch), `MGET` per key, `STRLEN`.
   Write paths: `SET` without TTL **clears** any previous expiry (set to 0).

3. **Command semantics**

    * `EXPIRE key seconds`: set `expireAtMillis = now + seconds*1000` if key exists; return 1 on success, 0 otherwise.
    * `PEXPIRE key ms`: set `expireAtMillis = now + ms` if key exists; return 1 or 0.
    * `TTL key`: if key missing → −2; if no ttl → −1; else floor((expireAtMillis − now)/1000) but never negative (return 0 when past).
    * `PTTL key`: same as TTL but in milliseconds.
    * `PERSIST key`: if key has ttl → clear to 0 and return 1; else 0.

4. **Metrics**

    * Counter: `ttl_lazy_expired_evictions` (increment each time a read deletes an expired key).
    * Gauge (optional): `ttl_keys_with_expiry` (increment/decrement on TTL set/cleared).
    * Optionally count calls: `cmd_expire_count`, `cmd_pexpire_count`, `cmd_ttl_count`, `cmd_pttl_count`, `cmd_persist_count`.

5. **INFO additions**
   Append a small TTL section or flat fields without renaming existing ones. Example fields:
   `"ttl_lazy_expired_evictions": <n>, "ttl_keys_with_expiry": <n>` (gauge optional).

6. **Edge cases**

    * Setting negative or zero TTL: treat ≤0 as immediate expiry (delete or set expired timestamp) and return 1 if key existed.
    * `EXPIRE` on non-existing key: return 0, do not create.
    * Overflow protection: clamp `expireAtMillis` to `Long.MAX_VALUE` on large inputs.
    * Clock skew: all relative to `System.currentTimeMillis()`; tests should not assume wall-clock precision.
    * Concurrent writes: SET after EXPIRE should clear TTL unless a variant explicitly preserves it (out of scope).

7. **Interfaces & touch points**

    * Storage entry gains `expireAtMillis` and helper `isExpired(now)`; caller enforces deletion.
    * Dispatcher wires new commands; arity checked as usual; bump per-command counters.
    * Metrics facade: `onTtlLazyEvict()`, `onCmdExpire()`, etc.

## Acceptance Criteria

* Expired keys never return from GET/EXISTS/MGET/STRLEN; reads delete expired keys immediately and count `ttl_lazy_expired_evictions`.
* `EXPIRE/PEXPIRE` set TTL only for existing keys; `PERSIST` clears TTL; `TTL/PTTL` reflect remaining time with specified sentinel values.
* `SET` without TTL clears prior expiry; `GETSET` returns nil for expired-as-deleted keys.
* `INFO` shows TTL metrics; counters monotonic; gauge (if implemented) accurate under add/remove flows.
* No changes to existing metric names; only additive fields.

## Manual Test Script

1. `SET k v` → `EXPIRE k 1` → sleep 1200ms → `GET k` returns nil; `ttl_lazy_expired_evictions` += 1.
2. `SET k v` → `TTL k` ≈ −1 → `EXPIRE k 10` → `TTL k` in [9..10]; `PTTL` in [9000..10000].
3. `SET k v` → `EXPIRE k 0` (or negative) → immediate delete; returns 1.
4. `SET k v` → `EXPIRE k 10` → `SET k v2` → `TTL k` = −1 (cleared).
5. `PERSIST` on key with no TTL → returns 0.

## Risks & Mitigations

* **Missed lazy deletes**: centralize the expiry check in a small set of read-entry points to avoid missing paths.
* **Clock jitter**: treat small negative TTL results as 0; never return negative elapsed time in TTL/PTTL (use sentinels).
* **Performance regressions**: expiry check is a single branch + compare; keep it branch-predictable and avoid extra locking.

## Done = Ready for Phase 3

* All acceptance criteria met; `INFO` extended; metrics behave under basic load.
* `progress.md` updated with delivered behaviors and any deferred edge cases.
