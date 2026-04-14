# Phase 4 — Storage Engine Hardening (Detailed)

> Timebox: Mar 2026 (~4 weeks @ 10–15h/week)
> Target outcome: robust in-memory store semantics with atomic RMW operations, multi-key convenience, and clearer error taxonomy. Single-node consistency first.

## Scope (what ships in Phase 4)

* Strengthen the core dictionary and value semantics.
* Implement atomic read-modify-write (RMW) operations.
* Add multi-key helpers and keyspace visibility.
* Broaden error taxonomy in the dispatcher (type/range errors).

## Non-Goals (defer)

* Persistence, replication, clustering.
* Complex data types beyond strings.

## Milestones

1. M1 — Atomic ops (EXISTS, SETNX, GETSET) (week 1)
2. M2 — Numeric ops (INCR/DECR/INCRBY/DECRBY) with range checks (week 2)
3. M3 — Multi-key helpers (MGET/MSET) + DBSIZE (week 3)
4. M4 — Error taxonomy, metrics, and `INFO` extensions (week 4)

## Work Breakdown (sequence)

1. **Dictionary semantics**

    * Ensure all mutations are mutually exclusive per key (synchronize at the right granularity; you can start with a coarse lock and refine to segmented locks later).
    * Define clear rules for overwrites, creation, and TTL interactions (SET clears TTL unless variant says otherwise).

2. **Atomic ops**

    * **EXISTS key**: return 1 if present and not expired (lazy expiry applies), else 0.
    * **SETNX key value**: set only if absent; return 1 if set, 0 otherwise.
    * **GETSET key value**: atomically replace and return old value or nil.

3. **Numeric ops**

    * **INCR/DECR**: treat missing as 0; values must be valid integer encodings.
    * **INCRBY/DECRBY**: 64-bit signed arithmetic; detect overflow and return a type/range error without mutation.
    * Counters: `cmd_incr_count`, etc.; error counters: `op_wrongtype_errors`, `op_range_errors`.

4. **Multi-key convenience & keyspace**

    * **MGET**: return array of bulk/nil with lazy expiry per key.
    * **MSET**: set multiple pairs; atomicity can be best-effort per pair for now; document behavior.
    * **DBSIZE**: exact count or approximate (choose and document); respects lazy expiry.

5. **Error taxonomy (dispatcher)**

    * Introduce operation-level errors distinct from protocol:
      `WRONGTYPE` (e.g., numeric op on non-integer), `RANGE` (overflow/underflow), `ARITY` (already implemented), `NOTSUPPORTED` (future placeholders).
    * Map them to clear error frames and bump counters: `op_wrongtype_errors`, `op_range_errors`.

6. **Metrics & INFO updates**

    * Add `cmd_exists_count`, `cmd_setnx_count`, `cmd_getset_count`, `cmd_incr_count`, `cmd_decr_count`, `cmd_incrby_count`, `cmd_decrby_count`, `cmd_mget_count`, `cmd_mset_count`, `cmd_dbsize_count`.
    * Add `op_wrongtype_errors`, `op_range_errors`.
    * Keep existing metric names stable; only additive fields.

## Acceptance Criteria

* All new commands behave atomically per key; no lost updates under concurrent access on a single node.
* Numeric ops reject non-integer values and detect overflow; store remains unchanged on error.
* MGET/MSET behave per spec; DBSIZE returns plausible numbers and drops expired keys lazily.
* TTL semantics integrate cleanly: GETSET also lazily expires; SETNX clears old TTL on write.

## Manual Test Script

1. `SETNX k v` twice → returns 1 then 0; `EXISTS k` = 1.
2. `GETSET k v2` returns `v`; `GET k` returns `v2`.
3. `INCR` on missing key → 1; `INCRBY` 9223372036854775807 → RANGE error and no mutation.
4. `MSET a 1 b 2` then `MGET a b c` → returns `1 2 nil`.
5. `DBSIZE` increases with writes and drops after deletes/expiry.

## Risks & Mitigations

* **Coarse locking bottlenecks:** start simple; add segmented locks or striped maps if contention shows up in benchmarks.
* **Ambiguous numeric parsing:** define and document acceptable integer encoding (ASCII decimal, optional leading `-`, no spaces).
* **Atomicity expectations for MSET:** document per-pair semantics now; revisit transactional variants later if needed.

## Done = Ready for Phase 5

* Commands implemented and stable; error taxonomy extended; metrics wired; `INFO` updated.
* `progress.md` updated with delivered features and any deferrals.
