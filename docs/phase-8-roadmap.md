# Phase 8 — Sharding / Cluster Basics (Detailed)

> Timebox: Sep 2026 (~4 weeks @ 10–15h/week)
> Target outcome: partition the keyspace across multiple nodes with simple routing and basic resharding; minimal cluster health view.

## Scope (what ships in Phase 8)

* **Keyspace partitioning:** fixed slot space (e.g., 16,384 slots) using a stable hash (CRC16 or similar).
* **Routing:** clients send to any node; node routes or redirects to the slot owner.
* **Cluster metadata:** static config first; in-memory view of nodes → slots.
* **Reshard/migration:** move slots (or specific keys) between nodes without full downtime.
* **Health view:** minimal cluster status via `INFO` or `CLUSTER NODES`.

## Non-Goals (defer)

* Automatic rebalancing; sophisticated failure detection or elections.
* Cross-slot transactions or multi-key atomicity.

## Milestones

1. M1 — Slot hashing + ownership map (week 1)
2. M2 — Request routing/redirect (week 2)
3. M3 — Basic reshard operation (week 3)
4. M4 — Cluster health reporting & hardening (week 4)

## Work Breakdown (sequence)

1. **Slot hashing**

    * Define `SLOTS = 16384` (or 4096 for simplicity).
    * Hash function over key bytes (CRC16 or a fast 64-bit mix); map to slot via modulo.
    * Optionally support hash tags (e.g., `{user:42}`) to force keys into the same slot for client-side grouping.

2. **Ownership map**

    * Static mapping at startup (config file listing nodes and their slot ranges).
    * In-memory table: `slot → nodeId`; and `nodeId → (host, port)`.

3. **Routing / redirect**

    * On each request, compute slot; if this node owns it, handle normally.
    * If not, either:
      **A. Proxy:** forward the request to the owner and relay the response (simplest UX).
      **B. Redirect:** return an error with the owner location; client reconnects (simpler server).
    * Start with **proxy** for ease of use; add redirect later if needed.

4. **Resharding/migration**

    * Admin op to move a slot (or a key set) to a new owner: source streams keys/values/TTLs to target; then flips ownership of that slot.
    * During move, proxy writes to the **new** owner (or block writes briefly) to avoid split-brain.
    * Keep ordering per key; ensure idempotent apply on target.

5. **Cluster view & health**

    * `INFO` adds a `cluster` section: role, known nodes, owned slots, moving slots.
    * Optional: `CLUSTER NODES` returns a simple text/JSON description of node IDs, addresses, slots, and states.

6. **Metrics**

    * `cluster_nodes`, `cluster_slots_total`, `cluster_slots_owned`.
    * `cluster_route_proxied`, `cluster_route_redirected`.
    * `migrate_moves_started`, `migrate_moves_completed`, `migrate_moves_failed`, `migrate_bytes_sent`, `migrate_bytes_received`.

## Acceptance Criteria

* Requests are consistently routed to the correct owner based on slot hashing.
* During a reshard of one slot, keys migrate and subsequent reads/writes land on the new owner with at most brief disruption.
* `INFO`/`CLUSTER NODES` reflect accurate topology and slot ownership.
* Metrics show routing activity and migration counts/bytes.

## Manual Test Script

1. Start 3 nodes with a static slot map; write/read random keys; verify distribution roughly uniform.
2. Move a slot from node A to B; ensure keys become available on B; verify minimal downtime.
3. Force requests to a non-owner; verify proxying (or client redirect) works and metrics increment.

## Risks & Mitigations

* **Hot slots / uneven hash:** allow manual slot remapping; support hash tags for grouping.
* **Proxy becomes bottleneck:** prefer direct-to-owner once clients are cluster-aware; add redirect path later.
* **Migration races:** fence with a slot state `MOVING`; block or proxy writes during the flip.

## Done = Ready for Phase 9

* Cluster routing and basic resharding work; health view present; `progress.md` updated.
