# Phase 3 — IO Model & Thread Pool (Detailed)

> Timebox: Jan–Feb 2026 (~6–8 weeks @ 10–15h/week)
> Target outcome: per-connection reader with robust framing, custom worker pool for command execution, bounded resources, and backpressure. No virtual threads.

## Scope (what ships in Phase 3)

* **Connection model:** one dedicated reader per connection (regular thread) that sequences parsing.
* **Framing:** replace `InputStreamWrapper` with a **per-connection read buffer + cursor** (bounded, reusable).
* **Execution model:** dispatch parsed requests to a **custom thread-pool executor** (regular threads) for command execution and response creation.
* **Backpressure:** bounded work queue; reject policy; pause reading when saturated.
* **Admission control:** `maxClients` gate on accept.
* **Metrics:** thread-pool (active workers, queue size, tasks enqueued/completed, rejected), admissions refused, read syscalls (optional).

## Non-Goals (defer)

* NIO/selector or async IO.
* TLS.
* Persistence/replication.

## Milestones

1. M1 — Executor skeleton + metrics (week 1)
2. M2 — Per-connection read buffer + cursor; parser adapts to cursor (week 2)
3. M3 — Reader→executor handoff (single response path) (week 3)
4. M4 — Bounded queue + backpressure + refusal policy (week 4)
5. M5 — Admission gate (`maxClients`) + refused counter (week 5)
6. M6 — Robust shutdown & leak checks + naming freeze for new metrics (week 6)

## Work Breakdown (sequence)

1. **Custom Executor**

    * Fixed-size worker threads; daemon=false.
    * Bounded queue with reject policy (e.g., `CallerRuns` or `RejectAndCount`).
    * Metrics: `tp_workers_max`, `tp_workers_active`, `tp_queue_size`, `tp_tasks_enqueued`, `tp_tasks_completed`, `tp_rejected`.

2. **Per-connection Read Buffer + Cursor**

    * Reusable byte array; start 8 KiB, grow geometrically to hard cap (e.g., 64–256 KiB).
    * Maintain `head`/`tail` indices; `fill()` from socket; parse via `cursor` methods; compact or ring-buffer strategy when needed.
    * Enforce max frame size and abort with protocol error if exceeded.
    * Parser remains stateless; takes a `Cursor` view for a single frame; returns consumed length.

3. **Reader Thread & Handoff**

    * Reader loop: `fill buffer` → `while (frameAvailable) parse` → **enqueue task** to executor containing the immutable request + connection write handle.
    * Maintain **per-connection request ordering** if required (see Ordering below).
    * Count `bytesIn` via head advance; unknown/arity errors stay as in Phase 1.

4. **Writer / Response Path**

    * Worker executes command, builds response, and **safely writes** back.
    * Decide on ordering model:
      **A. Ordered per-connection (recommended for Phase 3):** each connection has an output queue; only one in-flight write per connection; tasks complete in arrival order.
      **B. Out-of-order allowed:** only if protocol and clients tolerate it (usually not); skip for now.
    * `bytesOut` increments after full flush.

5. **Backpressure & Saturation**

    * When executor queue is **near full**, the reader should **pause reading** (stop `fill()`), leaving bytes in kernel buffer; resume when queue drains.
    * Reject policy increments `tp_rejected`; reader converts rejections into proper error responses or connection close (configurable).
    * Consider simple watermarking: `high_watermark` to pause reads; `low_watermark` to resume.

6. **Admission Control**

    * Track `current_clients`; if `>= maxClients` on accept, **refuse** connection and increment `connections_refused`.
    * Return a small error banner before close (optional) to aid debugging.

7. **Shutdown Semantics**

    * Graceful stop: stop accepting, drain executor with timeout, close remaining connections.
    * Ensure idempotent close paths; no negative gauges; verify no task leaks.

8. **Metrics & INFO Extensions**

    * Add fields (snake_case if you standardized it):
      `tp_workers_max`, `tp_workers_active`, `tp_queue_size`, `tp_tasks_enqueued`, `tp_tasks_completed`, `tp_rejected`, `connections_refused`.
    * Optional: `read_syscalls` to track buffer efficiency.
    * Keep existing fields untouched.

## Ordering Model (decision)

* **Phase 3 default:** **in-order per connection**. Each connection has a small output queue and a single writer token to serialize responses. Simpler and matches most clients’ expectations. We can revisit out-of-order later.

## Acceptance Criteria

* Server sustains multiple concurrent clients performing mixed GET/SET/DEL without correctness regressions.
* Memory usage remains bounded per connection (buffer caps honored).
* Under load, executor queue does not grow unbounded; backpressure engages; `tp_rejected` stays 0 in normal operation and >0 under synthetic overload.
* `connections_refused` increments when client count exceeds `maxClients`.
* No interleaving or corruption on writes; per-connection ordering is preserved.
* Clean shutdown drains or times out gracefully; no thread leaks; gauges return to baseline.

## Manual Load Smoke

* Open N clients (e.g., 100), issue pipelined GET/SET mixes; watch `tp_workers_active`, `tp_queue_size`, `bytes_in/out`, `cmd_*_count` grow; verify latencies stay reasonable.
* Force saturation with a tiny pool/queue; verify reads pause and `tp_rejected` increments if reject policy kicks in.
* Hit `maxClients` with connection storms; verify `connections_refused` increments and server stays stable.

## Risks & Mitigations

* **Deadlocks on write ordering:** keep one writer token per connection; avoid waiting while holding locks.
* **Excessive copies:** prefer ring buffer or careful compaction; avoid realloc thrash.
* **Head-of-line blocking:** long commands starve others—keep pool size > 1; consider separating read threads from workers.
* **Busy loops:** ensure reader sleeps/block reads when no data and when backpressured.

## Done = Ready for Phase 4

* All acceptance criteria met; metrics and `INFO` extended; backpressure validated; no leaks in thread or connection accounting.
