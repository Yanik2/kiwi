# Phase 1 — Core Protocol & Observability MVP (Detailed)

> Timebox: Oct–Nov 2025 (~6–8 weeks @ 10–15h/week)
> Target outcome: stable request/response protocol, minimal command set, metrics & INF, single-thread server acceptable.

## Scope (what ships in Phase 1)

* Protocol framing (method, key length, value length, CRLF separator) and error frames.
* Commands: GET, SET, DEL (single key), INFO/INF, EXIT (optional PING if you want it).
* Observability MVP: connection counters, bytes_in/out, per-command counts, protocol error buckets (unknown, arity), INF response.
* Idempotent close path; input-side wrapper acceptable (per-connection buffer deferred).

## Non-Goals (defer to later phases)

* TTL semantics and commands.
* Multi-key DEL beyond trivial loop.
* Thread pool / multi-threaded IO.
* Persistence, replication, cluster.

## Milestones

1. M1 — Skeleton server online + minimal INF (week 1)
2. M2 — GET/SET/DEL working with error frames (week 2)
3. M3 — Metrics: connections + clients gauge + INF dump (week 3)
4. M4 — Metrics: bytes_in/bytes_out (week 4)
5. M5 — Per-command counters + unknown/arity error buckets (week 5)
6. M6 — Protocol polish + hard limits + naming freeze for INF (week 6)

## Work Breakdown (sequence)

1. **Protocol framing & constants**
   Define: method size, length header max digits, key/value max bytes, separator.
   Add centralized constants and a single error frame encoder.

2. **Request parsing & error taxonomy**
   Keep parser stateless. On errors, throw `ProtocolException(code)` with `ProtocolErrorCode` enum (UNKNOWN_METHOD, HEADER_LEN_TOO_LONG, NON_DIGIT_IN_LENGTH, EOF_IN_LENGTH, KEY_TOO_LONG, VALUE_TOO_LONG, SEPARATOR_MISMATCH).
   Dispatcher handles arity; parser handles framing only.

3. **Command handlers (GET/SET/DEL/INFO/EXIT)**
   Minimal storage map; DEL single-key for now. INFO returns JSON. EXIT closes the connection gracefully.

4. **Metrics registry + INF v1**
   Metrics (eager singleton or DI registry): `acceptedConnections`, `closedConnections`, `currentClients`.
   INF dumps these fields as a single JSON object (stable order, snake_case later if you decide).

5. **Bytes accounting**
   `bytesIn`: increment after a full request is parsed (protocol-frame bytes).
   `bytesOut`: increment after a full response is flushed (include error frames).
   Keep all-or-nothing policy on partial writes.

6. **Command counters**
   Increment after successful dispatch: get/set/del/info/ext.
   Add `unknownRequests` and `arityErrors` counters (arity checked in dispatcher).

7. **Close path & invariants**
   Idempotent close hook; guard `currentClients` from going negative.
   Add lightweight assertions/invariants you can enable in dev mode.

8. **INF naming freeze**
   Choose final field names (you said keep current for now).
   Document the contract: counters are since-start; gauges are exact-now; JSON only for INFO.

## Acceptance Criteria

* GET/SET/DEL return correct frames; EXIT closes cleanly.
* Parser rejects malformed frames with protocol error; unknown and arity errors are separated.
* Metrics monotonic: accepted/closed, bytes_in/out, per-command counts, error buckets.
* First INF after startup may show bytes_out=0 for its own response (by design).
* No negative `currentClients`; close hook runs once per connection.
* Max key/value and header lengths enforced; violations produce protocol errors.

## Interfaces & Touch Points

* **Parser:** InputStreamWrapper in Phase 1; returns `TCPRequest` or throws `ProtocolException(code)`.
* **Dispatcher:** validates arity, routes to handlers, bumps per-command counters on success; bumps `arityErrors` on failure.
* **Writer:** returns `writtenBytes` for the frame; metrics bumps after successful flush.
* **MetricsRegistry/Facades:** seam methods like `onAccept()`, `onClose()`, `onParsedBytes(n)`, `onWriteBytes(n)`, `onCmdGet()`… `onProtoError(code)`.
* **INFO Renderer:** collects snapshot and formats JSON in stable order.

## Hard Limits (Phase 1)

* Max header digits: key=4 (≤ 4096), value=8 (≤ 10 MiB).
* Max overall frame size: implicit via the above; reject on overflow.
* Single-threaded accept/handle is fine.

## Metrics List (Phase 1 only)

* `acceptedConnections`, `closedConnections`, `currentClients`
* `bytesIn`, `bytesOut`
* `getRequests`, `setRequests`, `deleteRequests`, `infoRequests`, `exitRequests`
* `unknownRequests`, `arityErrors`

## Manual Test Script (no automated tests yet)

1. Start server; INF → zeros everywhere.
2. SET k v; GET k; DEL k; GET k → verify responses and counters.
3. Send `FOO` → unknownRequests increments; correct error frame.
4. Send `SET key` (missing value) → arityErrors increments.
5. Trigger oversized key/value → protocol error counters increment; connection closes if policy dictates.
6. Re-run INF and verify bytes_in/out grew as expected; clients gauge returns to baseline.

## Risks & Mitigations (Phase 1)

* **Metric drift on double-close:** fixed via idempotent close hook.
* **Overcount on bytes_out:** avoid by counting only after flush.
* **Parser over-consumption:** acceptable in Phase 1; per-connection buffer deferred.

## Done = Ready to cut Phase 1 tag

* All acceptance criteria met.
* `INFO` schema frozen (documented).
* `progress.md` updated with what shipped + known deferrals.
