# Phase 5 — Config & Admin Surface (Detailed)

> Timebox: Apr 2026 (~4 weeks @ 10–15h/week)
> Target outcome: externalized configuration, stable admin surfaces, and structured diagnostics without third‑party libs.

## Scope (what ships in Phase 5)

* Config ingestion at bootstrap with precedence: JVM `-D` > env vars > `kv.properties` > defaults.
* Immutable `Config` snapshot shared across subsystems; no hot reload.
* Metrics toggle (`metrics.enabled`) for fast no‑op.
* Admin commands: `CONFIG GET` (read‑only), `INFO` extended with effective config.
* Structured logging with connection/request context.

## Non-Goals (defer)

* `CONFIG SET` runtime mutation (optional later, read‑only first).
* Log shipping/rotation; external observability stacks.

## Milestones

1. M1 — Config loader + validation (week 1)
2. M2 — Wire config into server bootstrap and subsystems (week 2)
3. M3 — Structured logs with conn_id/req_id via `System.Logger` (week 3)
4. M4 — `CONFIG GET` and `INFO` config section; naming freeze (week 4)

## Work Breakdown (sequence)

1. **Config file & precedence**

    * Default path: `./config/kv.properties`; override via `-Dkv.config=/path/to/file`.
    * Keys to support now:
      `server.port`, `server.backlog`, `server.maxClients`, `socket.soTimeoutMillis`, `metrics.enabled`.
    * Precedence: System props > Env vars > File > Defaults.
    * Validation: port range, non‑negative timeouts, `maxClients ≥ 1`.

2. **Immutable `Config` snapshot**

    * Build on bootstrap; pass to subsystems.
    * No setters; no hot reload.
    * Metrics registry caches `metrics.enabled` for fast short‑circuit.

3. **Logging with context**

    * Use `System.Logger` (JEP 264) or `java.util.logging`.
    * Include: timestamp, level, `conn_id`, `req_id`, remote address, `cmd`, `result`, `reason`.
    * Keep output flat key=value for grep‑ability; avoid JSON.
    * Log on accept/close, parse error, timeout, unknown/arity errors; keep success logs at INFO/DEBUG depending on noise tolerance.

4. **Admin commands**

    * `CONFIG GET <key|*>`: return key/value pairs from effective `Config`. Read‑only in this phase.
    * `INFO`: extend with a `config` section (or flat keys) showing effective values; do not rename existing metrics fields.

5. **Metrics additions**

    * Add `connections_refused` if not yet present (gated by `maxClients`).
    * Consider `logs_emitted` counter (optional) only if cheap.

## Acceptance Criteria

* Server boots with each precedence level overriding correctly; invalid configs fail fast with a clear message.
* `metrics.enabled=false` disables metric increments with negligible overhead.
* Logs include `conn_id` and `req_id` across accept/close and error paths.
* `CONFIG GET *` returns all known keys and values; `INFO` includes effective config.
* No schema churn for existing `INFO` fields; only additive changes.

## Manual Test Script

1. Run with only defaults; observe `INFO` config section.
2. Add `kv.properties`; change `server.port`; verify bind on the new port.
3. Override with `-Dserver.port=...`; confirm precedence.
4. Set `metrics.enabled=false`; send traffic; counters remain unchanged.
5. Trigger unknown/arity errors; verify structured logs carry context.

## Risks & Mitigations

* **Config drift:** centralize key names and keep a single parser; reject unknown keys with a warning.
* **Logging performance:** keep logs sparse on hot path; avoid string concatenation by using parameterized messages if you adopt JUL.
* **Partial overrides:** print the full effective config at startup for visibility.

## Done = Ready for Phase 6

* Config/LOG/INFO surfaces stable; metrics toggle works; `progress.md` updated with outcomes and any deferrals.
