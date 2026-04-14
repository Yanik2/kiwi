Here’s a tight, phase-by-phase pass (Phases 2 → 9) with **relevant GoF patterns**, **where to use them**, and **why**. No code, just placements.

# Phase 2 — TTL Groundwork

* **Strategy** — expiry semantics: plug different policies (`lazy_only`, `lazy+active`, `immediate_delete_on_nonpositive`) behind a small interface the read/write paths call.
* **Decorator** — wrap the storage map with an **expiry-aware layer** that enforces “delete-on-read-if-expired” and TTL updates, keeping the core map simple.
* **Template Method** — standardize read paths: “fetch → check expiry → post-hook” so GET/EXISTS/STRLEN share the skeleton and only override the bit that formats the result.
* **Factory Method** — creating values/entries with or without TTL; factory normalizes `expireAtMillis` construction and validation.
* **Null Object** (lightweight) — represent “no TTL” as a null-object policy instead of a bunch of `if (ttl==0)` branches.

# Phase 3 — IO Model & Thread Pool

* **Command** — each parsed request becomes a command object enqueued to the pool (clean separation of parse/exec).
* **Strategy** — thread-pool rejection policy (`caller_runs`, `reject`, `block`), and backpressure policy (pause thresholds).
* **Template Method** — connection reader pipeline: “fill buffer → while(frame) parse → dispatch → after-hooks,” with overridable hooks for telemetry.
* **Proxy** — per-connection **writer proxy** to serialize responses in order (encapsulates single-writer token and buffering).
* **Object Pool** (not GoF; just note) — reuse byte buffers; if you want a GoF analog, **Flyweight** for small immutable protocol tokens (CRLF, fixed headers).

# Phase 4 — Storage Engine Hardening

* **Strategy** — per-key lock strategy (coarse lock vs segmented/striped), and numeric parsing strategy (strict/lenient).
* **Template Method** — atomic RMW ops share the skeleton: “load → validate type/range → transform → store → post-hook.”
* **Chain of Responsibility** — error classification: wrong type → range → arity; pass through validators until one handles.
* **Facade** — expose storage via a small, stable API (GET/PUT/DEL/RMW) while hiding concurrency/expiry details.
* **Memento** (optional) — if you support GETSET/rollback semantics for some ops later, memento holds the old value during a multi-step change.

# Phase 5 — Config & Admin Surface

* **Builder** — assemble immutable `Config` from layered sources (defaults → file → env → system props), with validation before `build()`.
* **Adapter** — unify env vars/system props/file into one `KeyValueSource` so the loader treats them uniformly.
* **Singleton** (scoped) — the **effective** `Config` snapshot (one instance), though accessed via DI.
* **Facade** — `AdminService` that fronts `INFO`, `CONFIG GET`, metrics snapshotting, logging toggles.
* **Bridge** — logging abstraction: decouple our structured log API from the underlying `System.Logger`/JUL so you could swap sinks later.

# Phase 6 — Expiry Engine & Memory Management

* **Strategy** — expiry sampling policy (random-sampling, min-heap, time-wheel) and eviction policy (`noevict`, `allkeys-lru`, later variants).
* **Observer** — optional: sampler publishes “expired/evicted” events that metrics subscribe to; keeps metrics out of hot delete path.
* **State** — sampler scheduler states: `IDLE → SAMPLING → BACKOFF`; transitions encode throttling behavior.
* **Template Method** — eviction workflow: “choose victims → remove → account → notify,” plugging different **Strategy** for victim selection.
* **Iterator** — key scanning for sampler and DBSIZE approximations without exposing internal map structure.

# Phase 7 — Replication (Leader–Follower)

* **Observer** — leader observes the mutation stream; followers subscribe to receive appended entries (fits your in-memory AOF-lite).
* **Command** — replication log entries are serialized command objects; follower **executes** the Command idempotently.
* **State** — link lifecycle (`DISCONNECTED/CONNECTING/HANDSHAKE/STREAMING/RESYNC`); behavior depends on state (timeouts, retries).
* **Template Method** — sync procedure skeleton: “handshake → full sync → tail stream”; hooks for persistence vs memory-only.
* **Proxy** — leader’s replication endpoint acts as a remote proxy for the command stream; follower applies locally.
* **Strategy** — resync policy (from offset vs full snapshot), backoff policy, and fencing policy (epoch handling).

# Phase 8 — Sharding / Cluster Basics

* **Strategy** — slot hashing function (CRC16, xxhash64→mod), and routing policy (proxy vs redirect).
* **Proxy** — server-side proxying to the slot owner (transparent forwarding); later you can flip to redirect without touching handlers.
* **Facade** — `ClusterView` encapsulates “slot→node” and node metadata behind a stable API.
* **Command** — admin ops like `MIGRATE SLOT`/`MOVE KEY` as command objects; can be queued/rolled back.
* **State** — slot migration state machine (`STABLE → MOVING → STABLE`) to fence reads/writes during moves.
* **Iterator** — stream keys of a slot during migration without loading all into memory.
* **Mediator** (optional) — if you end up with several components (router, migrator, health monitor), a mediator can coordinate without tight coupling.

# Phase 9 — Packaging, Deploy, Benchmarks

* **Builder** — benchmark scenario builder (mix %, value sizes, concurrency) to produce repeatable runs.
* **Template Method** — benchmark run skeleton: “warmup → steady state → cool-down → collect metrics.”
* **Facade** — packaging/deploy interface (`BuildService`, `DeployService`) that hides Gradle/Docker/GCP specifics behind a minimal surface.
* **Adapter** — wrap GCP tooling (gcloud/REST) behind a small interface if you script deployments in Java.
* **Singleton** — build metadata (git SHA, build time) registry shared across the process for `INFO`.

## Quick guardrails

* Prefer **Strategy + Template Method** for hot paths; they add clarity without allocations if you keep them static/final.
* Use **Proxy/Facade** at boundaries (network, admin, cluster) to keep the core small.
* Apply **State** only when behavior truly changes across phases of a component (replication link, slot migration, sampler).
* Keep **Observer** limited to low-rate events (replication/TTL/evictions) to avoid accidental fan-out in hot paths.

If you want, I can annotate your current classes with “candidate spots” for each pattern next.
