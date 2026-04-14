# Phase J — JVM Deep Dive & Hardening (Detailed)

> Timebox: Jul 2026 (~4 weeks @ 10–15h/week)
> Target outcome: code-heavy JVM tooling + tuning defaults, with optional product integration via a dedicated package.

## Package layout (no external deps)
`com.yourorg.kv.jvm.{jfr,gc,buffers,executor,safepoint,startup,introspect}`

## Scope (what ships in Phase J)
1. **JFR & Introspection**
    - JFR controller (start/stop to rolling files) + 2–3 custom events (queue depth, request lifecycle).
    - `/info jvm` section from MXBeans (heap, GC, threads, cpu, uptime).

2. **Allocation & Buffers**
    - Pluggable **BufferManager** (heap vs direct) with pooling and leak tracking (scoped leases).
    - **Arena/Slab** allocator (on-heap) for temp slices on hot paths; poisoning in debug mode.
    - Key encoding **Strategy**: UTF-8 bytes, Latin-1 packed, or `String`.

3. **JIT/JMM/Queues**
    - **MPMC ring buffer** (VarHandles, power-of-two mask) with backoff policies and contention counters.
    - **False-sharing-resistant counters** (manual padding + `@Contended` variant for lab runs).
    - **Warmup gate & deopt detector** (consume JFR compilation/deopt events).

4. **Safepoints & Batching**
    - **Safepoint watchdog** (sleep drift deltas; P95/P99).
    - **Batch-size governor** to bound long loops (replication/apply later uses this).

5. **Startup**
    - **CDS/AppCDS** classlist + archive generation; measure startup and footprint.

## Non-Goals (defer)
* Changing external command APIs.
* Deep persistence; this phase is runtime- and hot-path–focused.

## Milestones
1. M1 — JFR controller + custom events + Gradle tasks.
2. M2 — MXBeans `/info jvm` surface; stable field names.
3. M3 — BufferManager (heap/direct) + pooling + leak tracker.
4. M4 — Arena allocator + toggle in hot paths.
5. M5 — Key encoding strategies + metrics toggles.
6. M6 — MPMC ring + padded counters + micro-harness.
7. M7 — Warmup gate + deopt detector + “steady-state” criteria.
8. M8 — Safepoint watchdog + batch governor hooks.
9. M9 — CDS/AppCDS pipeline + measurements.
10. M10 — Tuning playbook (defaults vs lab profile).

## Acceptance Criteria
* Default **GC + heap + pause** recommendation documented with JFR/GC evidence.
* **/info jvm** shows heap/GC/threads/cpu/uptime; costs negligible.
* Buffer/Arena toggles demonstrably reduce allocation on at least one write-heavy scenario.
* MPMC queue and counters integrate behind flags; JMM edges documented.
* Safepoint watchdog records spikes; batch governor bounds tail latency in synthetic loops.
* CDS tasks run and produce measurable startup deltas on your machine.

## Cross-Phase References
* Do Phase J **after Phase 6** (expiry/memory) and **before Phase 7** (replication) so link sizing and batching inherit the guardrails from here. See: Phase 6 expiry engine, Phase 7 replication, Phase 9 benchmarks for where Phase J outputs are consumed. 
