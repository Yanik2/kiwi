# KV Store — High-Level Development Plan (12 Months)

> Context: Pure Java 21, no external libs/frameworks; regular threads (no virtual), custom thread pool, Gradle, Google Cloud deploy target. ~10–15 hrs/week over ~12 months. Tests/docs last.

## Timeline at a Glance

* **Phase 1 (Oct–Nov 2025): Core protocol & observability MVP**
* **Phase 2 (Dec 2025): TTL groundwork**
* **Phase 3 (Jan–Feb 2026): IO model & thread pool**
* **Phase 4 (Mar 2026): Storage engine hardening**
* **Phase 5 (Apr 2026): Config & admin surface**
* **Phase 6 (May–Jun 2026): Expiry engine & memory management**
* **Phase J (Jul 2026): JVM Deep Dive & Hardening**   ← new
* **Phase 7 (Aug–Sep 2026): Replication (single leader + follower)**
* **Phase 8 (Oct 2026): Sharding/cluster basics**
* **Phase 9 (Nov 2026): Packaging, deploy, benchmarks**


> Dates are targets; slip is fine as long as exit criteria per phase are met.

---

## Phase 1 — Core Protocol & Observability MVP (Oct–Nov 2025)

**Goals**

* Stable wire protocol; request/response lifecycle; error framing.
* Metrics foundation (counters/gauges) + `INFO` command.

**Deliverables**

* Commands: `GET`, `SET`, `DEL`, `INFO`, `EXIT` (+ `PING` optionally).
* Metrics: connections, bytes_in/out, per-command counts, protocol error buckets (unknown/arity).
* Idempotent close path, single-thread server OK.

**Exit criteria**

* `INFO` exposes stable, parseable snapshot; counters monotonic.
* Manual smoke passes for happy path and basic error cases.

---

## Phase 2 — TTL Groundwork (Dec 2025)

**Goals**

* Make values expiry-aware; enforce lazy expiry on reads/writes.

**Deliverables**

* Data model with `expireAtMillis`.
* Semantics: GET deletes-on-expired; SET clears old TTL unless specified.
* Stubs: `EXPIRE/PEXPIRE/TTL/PTTL/PERSIST` (may return "not implemented" initially).
* Metrics: `ttl.lazy_expired_evictions`, `ttl.keys_with_expiry` gauge (optional).

**Exit criteria**

* No expired key leaks on reads; lazy eviction counter grows under tests.

---

## Phase 3 — IO Model & Thread Pool (Jan–Feb 2026)

**Goals**

* Move from blocking single thread to per-connection reader + worker pool for execution.

**Deliverables**

* Custom executor (regular threads), bounded queue, backpressure policy.
* Per-connection read buffer + cursor; pipelining-safe framing.
* Admission gate: `maxClients`; metrics for pool (active, queued, rejected).

**Exit criteria**

* Sustained concurrent clients without correctness regressions; no unbounded memory growth.

---

## Phase 4 — Storage Engine Hardening (Mar 2026)

**Goals**

* Solid in-memory dictionary semantics; atomic RMW ops; basic data validation.

**Deliverables**

* Atomic ops: `EXISTS`, `SETNX`, `GETSET`, `INCR/DECR/INCRBY/DECRBY`.
* Multi-key: `MGET/MSET`; keyspace metrics; approximate `DBSIZE`.
* Error codes: wrong type, range/overflow.

**Exit criteria**

* Linearizable behavior within a single node; per-command invariants hold.

---

## Phase 5 — Config & Admin Surface (Apr 2026)

**Goals**

* Externalize knobs; consistent admin commands.

**Deliverables**

* `kv.properties` (with system/env override): port, backlog, timeouts, maxClients, metrics.enabled.
* `CONFIG GET/SET` (optional, read-only first). Structured logs with conn_id/req_id.

**Exit criteria**

* Service boots from config; `INFO` shows effective config; logs carry connection context.

---

## Phase 6 — Expiry Engine & Memory Management (May–Jun 2026)

**Goals**

* Add active expiry sampling; enforce max memory (optional) and eviction policy.

**Deliverables**

* Background sampler to proactively delete expired keys.
* Eviction policy (optional): `noevict` → `allkeys-lru` as a starter.
* Metrics: active expired evictions, eviction scans/removals.

**Exit criteria**

* Expired keys disappear without access; memory stays below target under load.

---

## Phase J — JVM Deep Dive & Hardening (Jul 2026)

**Why here?** We’ve just landed expiry and memory guardrails (Phase 6). Before we layer on replication (Phase 7) and clustering (Phase 8), we’ll harden GC/JIT/JMM behavior and lock in defaults.

**Goals**
* Build actionable understanding of GC, JIT warmup, the Java Memory Model, and safepoints for our KV workload.
* Produce a small, repeatable tuning playbook (flags + rationale) and JFR/metrics baselines.
* Implement JVM-focused components that the product can optionally use (kept in a dedicated package).

**Deliverables (code-heavy)**
* **JFR controller + custom events** (queue depth, request lifecycle).
* **Unified GC log parser** (streaming; pause/promotion stats).
* **/INFO jvm** snapshot via MXBeans (heap, GC, threads, cpu, uptime).
* **Buffer manager** (heap vs direct) with pooling + leak tracking; **arena/slab** allocator (on-heap).
* **Key encoding strategies** (bytes/Latin-1/String) behind a toggle.
* **MPMC ring buffer** (VarHandles) + contention/backoff policies; **padded counters**.
* **Warmup gate & deopt detector** (reads JFR events).
* **Safepoint watchdog** + **batch-size governor** hooks.
* **CDS/AppCDS** build/run tasks and measurements.

**Exit criteria**
* Chosen default GC/heap/pause target with numbers to justify.
* Hot paths allocate minimally or intentionally; baselines captured (JFR + GC logs).
* Clear JMM happens-before edges documented for I/O → executor → writer.
* `/INFO` exposes a low-cost “jvm” section; all new code ships under `com.yourorg.kv.jvm.*`.

**Notes**
* Phases 7–9 reference this phase for tuning prerequisites (replication link sizing, routing hot path hashing, benchmark warmup gates). See the detailed phase docs for those phases.

---

## Phase 7 — Replication (Jul–Aug 2026)

**Goals**

* Single leader with one or more followers; at-least-once delivery semantics acceptable initially.

**Deliverables**

* State transfer (full sync) + incremental stream.
* Link health, offsets, lag metrics.
* Simple failover manual procedure (automatic later).

**Exit criteria**

* Followers converge on leader under steady load; tolerable lag; reconnection works.

---

## Phase 8 — Sharding/Cluster Basics (Sep 2026)

**Goals**

* Partition keyspace across nodes; minimal routing and rebalancing.

**Deliverables**

* Slot hashing (e.g., 16K slots); consistent hashing ring or CRC16 scheme.
* Simple reshard/migrate command; basic cluster health view.

**Exit criteria**

* Reads/writes routed to correct owner; resharding moves data without downtime for small sets.

---

## Phase 9 — Packaging, Deploy, Benchmarks (Oct 2026)

**Goals**

* Productionize: binaries, observability, load tests, cloud deployment.

**Deliverables**

* Gradle packaging (fat JAR), Dockerfile (if desired), startup scripts.
* Baseline benchmarks (throughput/latency at P50/P95). Dashboards (even if text-based).
* GCP deploy (Compute Engine or GKE) with basic runbook.

**Exit criteria**

* Reproducible build + deploy; published numbers and a demoable cluster.

---

## Risks & Mitigations (High-Level)

* **Scope creep:** freeze command set per phase; defer nice-to-haves.
* **Performance surprises:** measure each phase; add metrics before feature work.
* **Complexity explosion:** prefer simple, proven designs (per-conn reader + worker pool; additive metrics).

## Tracking

* Keep `progress.md` updated at the end of each phase with: delivered, deferred, known issues, and next phase entry criteria.
