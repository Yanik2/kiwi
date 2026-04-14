# Phase 6 — Expiry Engine & Memory Management (Detailed)

> Timebox: May–Jun 2026 (~6–8 weeks @ 10–15h/week)
> Target outcome: proactive expiry of dead keys and optional memory cap with a basic eviction policy; observability for both.

## Scope (what ships in Phase 6)

* **Active expiry engine:** background sampler that deletes expired keys without reads.
* **Memory guard (optional but recommended):** soft cap with a simple eviction policy (start with `noevict`, optionally add `allkeys-lru`).
* **Observability:** counters for active expirations, scans, and evictions; gauges for keys with TTL and memory use snapshots.

## Non-Goals (defer)

* Perfect LRU precision; fancy eviction policies.
* Persistence.

## Milestones

1. M1 — Sampler scaffold + scheduling (week 1)
2. M2 — Expiry index/queues and safe delete path (week 2)
3. M3 — Sampler heuristics + throttling under load (week 3)
4. M4 — Memory cap guardrail + `noevict` policy (week 4)
5. M5 — Optional `allkeys-lru` eviction + metrics (weeks 5–6)
6. M6 — INFO extensions + tuning dials (week 7)

## Work Breakdown (sequence)

1. **Scheduling model**

    * Use a dedicated background thread (or a small pool) for expiry sampling.
    * Periodic wakeups (e.g., every 100ms–1s) with adaptive timing based on work found.
    * Ensure cooperative shutdown.

2. **Expiry data access**

    * Start simple: sample N random keys from the main dictionary per cycle; delete if expired.
    * Optional index: maintain a min-heap or time-bucket wheel keyed by `expireAtMillis` to pull likely-expired keys faster.
    * Always check `now` at delete time to avoid clock drift issues.

3. **Delete semantics**

    * Use the same delete path as client `DEL` to keep accounting unified.
    * Respect per-key locks/segmentation to avoid long pauses.
    * Count `ttl_active_expired_evictions` for removals done by the sampler.

4. **Heuristics & throttling**

    * When many expired keys are found, increase batch size or reduce sleep to catch up.
    * When few are found, back off to avoid CPU burn.
    * Cap work per cycle to prevent starving request handling.

5. **Memory guard (optional)**

    * Track approximate memory usage (value sizes + overhead estimate).
    * Configurable `memory.max_bytes`; if exceeded under writes, apply policy:

        * `noevict`: reject write with an error (count `eviction_triggered` without removal).
        * `allkeys-lru` (optional): evict least-recently-used keys until under cap; count `eviction_removed`.

6. **Metrics & INFO**

    * Counters: `ttl_active_expired_evictions`, `ttl_scanned`, `eviction_triggered`, `eviction_removed`.
    * Gauges: `ttl_keys_with_expiry`, `memory_used_bytes` (approx), `memory_max_bytes` (config).
    * Keep existing fields stable; add a TTL/Eviction section in `INFO`.

7. **Configuration dials**

    * `ttl.sampler_period_ms`, `ttl.sample_batch`, `ttl.backoff_max_ms`.
    * `memory.max_bytes`, `eviction.policy` (`noevict`|`allkeys-lru`).

## Acceptance Criteria

* Expired keys disappear without reads; sampler activity visible in metrics (`ttl_active_expired_evictions` grows).
* Under write pressure with `noevict`, writes that would exceed the cap fail deterministically; with `allkeys-lru`, evictions happen and the store stays under the cap.
* Request latency remains acceptable; sampler yields under heavy foreground load.
* `INFO` shows TTL/Eviction stats and effective config values.

## Manual Test Script

1. Write 10k keys with short TTLs; stop reading; confirm sampler removes them over time; metrics reflect progress.
2. Set a low memory cap; write until over cap: with `noevict` see rejections; with `allkeys-lru` see evictions and steady state.
3. Under heavy foreground load, confirm sampler reduces its work (backoff) and request latency remains stable.

## Risks & Mitigations

* **Sampler fighting the workload:** enforce per-cycle work caps and adaptive backoff.
* **Inaccurate memory accounting:** use conservative estimates; document that values are approximate.
* **Eviction correctness:** keep LRU metadata simple (last-access timestamp or simple counter) and lock per segment to avoid global pauses.

## Done = Ready for Phase 7

* Sampler, metrics, and (optional) eviction behave as specified; `INFO` extended; config dials wired; `progress.md` updated.
