# Phase 9 — Packaging, Deploy, Benchmarks (Detailed)

> Timebox: Oct 2026 (~4 weeks @ 10–15h/week)
> Target outcome: reproducible builds, a deployable artifact, baseline performance numbers, and a simple cloud deployment.

## Scope (what ships in Phase 9)

* **Build & packaging:** Gradle tasks to produce a fat JAR (or modular JAR) and optional Docker image.
* **Runtime layout:** config directory, logs directory, start scripts (Unix/Windows).
* **Benchmarks:** repeatable local load tests with basic latency/throughput publishing.
* **Deployment:** minimal GCP setup (Compute Engine VM or GKE) + runbook.
* **Docs:** quickstart README and operator notes (even if brief).

## Non-Goals (defer)

* Full CI/CD pipelines; sophisticated dashboards.

## Milestones

1. M1 — Gradle packaging tasks + versioning (week 1)
2. M2 — Runtime layout + start scripts (week 2)
3. M3 — Benchmark harness + baseline numbers (week 3)
4. M4 — GCP deploy + runbook + README polish (week 4)

## Work Breakdown (sequence)

1. **Build & packaging**

    * Gradle tasks: `clean`, `build`, `shadowJar` (or equivalent) to produce a single runnable artifact.
    * Include build metadata in `INFO` (git SHA, build time).
    * Optional Dockerfile: minimal base (e.g., Eclipse Temurin JRE 21), copy fat JAR, set entrypoint; mount `/config` and `/logs`.

2. **Runtime layout**

    * Directories: `/config`, `/logs`, `/data` (future persistence).
    * Start scripts: `bin/kv-server` (Unix) and `bin/kv-server.bat` (Windows) accepting overrides (e.g., `-Dserver.port=...`).
    * Logging defaults and rotation policy (basic, no external deps).

3. **Benchmark harness**

    * Write a small Java client or use `wrk`-style generator to send mixed GET/SET workloads.
    * Scenarios: single connection pipelined, many connections, small/large values.
    * Report: throughput (ops/s), latency (P50/P95).
    * Metrics exported via `INFO` and captured after each run; store results in `/benchmarks`.

4. **Deployment (GCP)**

    * Compute Engine: VM with firewall rules for client and (if used) replication/cluster ports.
    * Or GKE: simple Deployment + Service; mount ConfigMap for `kv.properties`.
    * Runbook: startup flags, log locations, how to change port/timeouts, basic troubleshooting.

5. **Docs & polish**

    * `README.md`: quickstart, config keys, protocol synopsis, sample `INFO` output.
    * `OPERATIONS.md`: runbook, log meanings, common issues.
    * Update `progress.md` with final status and known gaps.

## Acceptance Criteria

* `./gradlew build` produces a runnable artifact; `java -jar kv-store.jar` starts the server.
* Optional Docker image runs with mounted config; logs written to `/logs`.
* Benchmark results are reproducible and recorded; you have baseline numbers for different scenarios.
* Service runs on GCP and is reachable; runbook steps validated.
* Documentation exists and matches current behavior.

## Manual Checklist

* Build artifact contains version and commit ID; `INFO` surfaces them.
* Start script supports overrides; config precedence works in the packaged runtime.
* Benchmarks complete without errors; `INFO` metrics align with load generator stats.
* GCP firewall rules correct; instance restarts cleanly; logs rotate as expected.

## Risks & Mitigations

* **Size bloat in fat JAR:** keep dependencies minimal (you already are).
* **JRE mismatch on GCP:** standardize on Temurin 21 JRE base; verify locally first.
* **Benchmark bias:** clearly document scenarios and versions; keep inputs and value sizes explicit.

## Done = Project Ready to Demo

* Packaged, deployable, benchmarked. You can show it running in the cloud with metrics and basic docs.
