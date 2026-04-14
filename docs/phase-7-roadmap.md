# Phase 7 — Replication (Leader–Follower) (Detailed)

> Timebox: Jul–Aug 2026 (~6–8 weeks @ 10–15h/week)
> Target outcome: one leader with one or more followers that maintain a near-real-time copy of the dataset. Start with at-least-once delivery and eventual consistency; refine later.

## Scope (what ships in Phase 7)

* **Topology:** single leader, N followers; static config of roles for first cut.
* **Sync:** full snapshot transfer (initial sync) + incremental command stream.
* **Link management:** connection lifecycle, heartbeats, offsets, and reconnection.
* **Safety:** idempotent apply on followers; simple fencing to avoid double leaders.
* **Observability:** replication link state, lag, offsets, and error counters.

## Non-Goals (defer)

* Automatic leader election/failover.
* Multi-leader or conflict resolution.
* Strong consistency or exactly-once delivery.

## Milestones

1. M1 — Role boot + follower link handshake (week 1)
2. M2 — Full sync (leader → follower snapshot) (week 2)
3. M3 — Incremental streaming of write commands (week 3)
4. M4 — Reconnect/resync logic with offsets (week 4)
5. M5 — Metrics + INFO replication section (week 5)
6. M6 — Hardening (timeouts, backpressure, idempotence) (week 6)

## Work Breakdown (sequence)

1. **Role & boot**

    * Config flags: `repl.role=leader|follower`, `repl.leader_addr`, `repl.leader_port`.
    * Leader listens on a replication port; follower dials leader on boot.

2. **Handshake & metadata**

    * Exchange protocol version, node ID, dataset epoch, and current leader offset.
    * Follower presents last applied offset (0 for fresh start).

3. **Full sync**

    * Leader builds a snapshot stream (key/value pairs with TTLs).
    * Follower clears local dataset, applies snapshot, and records `offset_at_snapshot` (or epoch).
    * Keep snapshot format simple (length-prefixed entries) and streaming; avoid buffering whole dataset.

4. **Incremental stream**

    * Leader appends each **mutating** command (SET/DEL/SETNX/INCR/...) to a replication log with a monotonically increasing offset.
    * Send the log tail to followers after full sync; include TTL changes.
    * Follower applies entries idempotently (duplicate offset = drop).
    * Backpressure: pause tailing if follower is behind or link congested.

5. **Reconnect & resync**

    * On disconnect, follower reconnects and requests from its last durable offset.
    * If the leader has truncated past that offset (log gap), fall back to full sync.
    * Timeouts and retries with exponential backoff.

6. **Safety & ordering**

    * Only the leader accepts writes; followers reject mutating commands with an error.
    * Ensure per-key order is preserved via log order.
    * Optionally fence: a simple epoch increases on leader restart; followers refuse lower-epoch streams.

7. **Persistence hook (optional for reliability)**

    * Keep the replication log in memory for first cut.
    * Optionally append to a file (AOF-lite) to survive leader restart across short outages.

8. **Metrics & INFO extensions**

    * Per link: `repl_link_state` (up/down), `repl_local_offset`, `repl_remote_offset`, `repl_lag_bytes`, `repl_fullsyncs`, `repl_partials`, `repl_resync_failures`.
    * Role snapshot: `repl_role` = `leader`/`follower`; number of followers.
    * Counters for errors/timeouts.

## Acceptance Criteria

* A follower bootstraps from an empty state and converges to the leader after full sync + incremental streaming.
* After transient disconnects, followers resume from the last offset or fall back to full sync when needed.
* Followers reject writes; reads reflect applied state; lag remains bounded under moderate load.
* `INFO` shows replication role, link states, offsets, and lag; counters move under failures.

## Manual Test Script

1. Start leader, write keys; start follower; verify it catches up.
2. Kill follower link mid-stream; restart follower; confirm resume from offset.
3. Rotate leader (restart) without persistence: expect full sync; with AOF-lite: resume from file.
4. Stress with steady writes; track lag growth and recovery.

## Risks & Mitigations

* **Log gap after leader restart:** add AOF-lite or shorten allowed outage window; otherwise trigger full sync.
* **Backpressure on leader:** cap per-follower send buffers; drop/slow followers that are too far behind.
* **Idempotence bugs:** use offsets as the single source of truth; drop duplicates; ensure apply is atomic per entry.

## Done = Ready for Phase 8

* Leader–follower replication works end-to-end with observability; resync logic validated; `progress.md` updated.
