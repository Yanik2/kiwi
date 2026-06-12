# Progress — KV Store

**Status:** Phase 1 (Core protocol & observability MVP) — **DONE / FROZEN (v1.0)**

## Implemented (Phase 1)

### Commands

* `GET`, `SET`, `DEL` (single-key)
* `INFO` (JSON payload)
* `EXT/EXIT` (graceful close)
* `PING` → `PONG`

### Wire Protocol

* Length‑prefixed **method token** (1 byte for method length, today capped at 4); method bytes are ASCII and validated.
* Framing for key/value: decimal length headers + `\r\n` separators; bulk bytes follow each header.
* Standard error frame on protocol/command errors.

### Metrics & Observability

* **Connections:** `acceptedConnections`, `closedConnections`, `currentClients`
* **Traffic:** `bytesIn` (after full parse), `bytesOut` (after full write)
* **Command counters:** per‑method counts (GET/SET/DEL/INFO/EXT/PING) and `unknownRequests`
* **Protocol error taxonomy:** `ProtocolErrorCode` attached to `ProtocolException`; per‑code counters recorded (e.g., `UNKNOWN_METHOD`, `METHOD_LEN_TOO_LONG`, `HEADER_LEN_TOO_LONG`, `NON_DIGIT_IN_LENGTH`, `EOF_IN_LENGTH`, `KEY_TOO_LONG`, `VALUE_TOO_LONG`, `SEPARATOR_MISMATCH`, `TRUNCATED_BULK`)
* **Admission control:** `maxClients` gate with `connectionsRefused` (refuse immediately post‑accept; silent close by default)
* **Uptime & versions:** `serverStartMillis`, `uptimeMillis`, `protocolVersion` (wire framing v1.0), `infoSchemaVersion` (INF schema v1.0)

### INFO (stable v1.0)

* Returns a single JSON object containing: connection counters, traffic counters, per‑command counters, protocol‑error buckets, admission metric, start/uptime, and the two version strings. Field names are frozen for v1.0.

### Limits (centralized constants)

* `max_method_len = 4`
* `max_key_len_bytes = 4096`
* `max_value_len_bytes = 10_485_760` (10 MiB)
* `max_header_digits_key = 4`
* `max_header_digits_value = 8`

## Runtime/Architecture (current)

* Single‑threaded, blocking server (`ServerSocket.accept()` loop).
* Per‑request parsing uses `InputStreamWrapper`; bytes accounting happens at handler boundaries.
* Idempotent close hook ensures `currentClients` never goes negative.

## Validation Notes

* First `INF` after startup may show `bytesOut = 0` for its own response (count happens after write completes).
* Parser guarantees request arity; arity errors are not emitted by dispatcher.

---

**Phase 1 closed.** Subsequent work will be tracked under Phase 2+ documents.

# Phase 2 — TTL Groundwork (Delivered)

* **Data model:** entries carry `expireAtMillis`; **`0` = no TTL**. Time base: `System.currentTimeMillis()`.
* **Lazy expiry gate (Storage):** all read/existence paths delete-on-access if expired and bump **`ttl_lazy_expired_evictions`**. GET returns “missing”; DEL on expired returns `0`.
* **Commands added:**

    * `EXPIRE key <seconds>` / `PEXPIRE key <ms>` set TTL only if the key exists.
    * `PERSIST key` clears TTL.
    * `TTL key` / `PTTL key` report remaining lifetime with sentinels **−2** (missing) / **−1** (no TTL); never negative (cap at 0).
* **Write rule:** plain `SET key value` **clears any existing TTL** (stores with no TTL).
* **Dispatcher & validation:** switched to **Command** registry and **per-command validators**; parser remains framing/bytes-only and supports variable arities.
* **INFO & metrics:** additive fields only; exposed **`ttl_lazy_expired_evictions`** (and optional **`ttl_keys_with_expiry`** gauge). Existing v1.0 names unchanged.
* **Edge cases locked:** non-positive TTL ⇒ **immediate delete** (returns 1 if key existed); overflow clamps `expireAtMillis` to `Long.MAX_VALUE`.

**Exit criteria:** no expired key leaks on reads; lazy-eviction counter moves under access; TTL/PTTL/PERSIST semantics match spec; INFO extended without schema churn. 

---

# Phase 3 — Concurrency, Networking & Stability (Delivered)

* **Thread pool execution model:** introduced custom **`KiwiThreadPool`** with bounded `ArrayBlockingQueue`, configurable workers, rejection policy, and runtime metrics (`tp.req.*`).
* **Incremental request parsing:** implemented per-connection **`ReadBuffer` + `Cursor`** allowing partial TCP reads, pipelined requests, and bounded frame sizes without parsing directly from `InputStream`.
* **Connection concurrency model:** moved from single-threaded handling to **thread-per-connection reader model**:

  * Accept loop creates **`ConnectionContext`**
  * Dedicated **`ConnectionReader`** thread performs blocking socket reads and incremental parsing
  * Parsed requests are submitted to the request executor (`KiwiThreadPool`)
* **Ordered response delivery:** implemented **`WriterProxy`** per connection:

  * Maintains request sequence numbers
  * Ensures responses are written strictly in request order
  * Prevents concurrent socket writes
* **Backpressure mechanism:** added **watermark-based backpressure gate**:

  * Reader threads pause when executor queue reaches `high_watermark`
  * Resume when queue falls below `low_watermark`
  * Prevents unbounded memory growth and request bursts
* **Per-connection fairness control:** introduced **in-flight request cap**:

  * Each connection has a bounded number of outstanding requests
  * Reader pauses parsing when limit is reached
  * Prevents single client from monopolizing executor queue
* **Graceful shutdown:** server lifecycle now supports deterministic shutdown:

  * stop accepting new connections
  * close active connections
  * drain writer proxies (best effort)
  * terminate reader threads and executors cleanly
* **Slow-client protection:** writer queues bounded; overload paths close connections deterministically to avoid unbounded buffering.
* **Observability extensions:** INFO and metrics expanded to include:

  * executor metrics (`tp.req.*`)
  * backpressure metrics (`bp.*`)
  * connection and traffic counters
  * fairness / rejection counters

## Runtime/Architecture (current)

* **Accept loop:** lightweight acceptor thread creating connection contexts.
* **Reader threads:** one blocking reader per connection for network I/O and parsing.
* **Worker pool:** bounded `KiwiThreadPool` executing command handlers.
* **Writer proxy:** single ordered writer per connection ensuring deterministic output.
* **Backpressure:** queue watermarks pause readers before memory growth.
* **Fairness:** per-connection in-flight limits prevent queue monopolization.

## Validation Notes

* Server remains stable under overload: readers pause via backpressure rather than allocating unbounded buffers.
* Strict response ordering preserved under parallel execution.
* Graceful shutdown closes connections and drains writers without deadlocks.
* Pipelined requests from a single client cannot starve other clients due to in-flight caps.

---

**Phase 3 closed.** The server now supports concurrent clients, ordered responses, bounded memory behavior under load, and deterministic shutdown semantics.

---

### Phase 4 — Atomic Ops & Keyspace Helpers

**Status:** DONE

**Summary:**
Phase 4 introduced atomic read–modify–write semantics, numeric operations, multi-key commands, and a clear separation between protocol and operation-level errors. Storage abstraction was refactored to enforce correctness boundaries and support concurrent access safely.

**Implemented:**

* **Storage Facade & Atomic Mutation**

  * Introduced `StorageFacade` as the single entry point
  * Implemented atomic `mutate()` with logical state (`CurrentState`)
  * Centralized lazy expiry handling
  * Enforced separation between storage internals and command logic

* **Locking Model**

  * Replaced coarse-grained lock with **striped locking**
  * Lock selection based on `key.hashCode() % N`
  * Improved concurrency while preserving per-key atomicity

* **Atomic Commands (M1)**

  * `EXISTS`
  * `SETNX`
  * `GETSET`

* **Numeric Commands (M2)**

  * `INCR`, `DECR`, `INCRBY`, `DECRBY`
  * Strict 64-bit integer parsing (ASCII)
  * Checked arithmetic (overflow → `RANGE`)
  * Missing key treated as `0`
  * TTL preserved for existing keys

* **Multi-key & Keyspace Commands (M3)**

  * `MGET` (ordered results, per-key lazy expiry)
  * `MSET` (best-effort per pair, documented)
  * `DBSIZE` (exact visible count under lazy expiry)

* **Binary Protocol Extension**

  * Added explicit **count header** for multi-key requests
  * Improved parser determinism and validation

* **Error Taxonomy (M4)**

  * Introduced operation-level errors:

    * `WRONGTYPE`
    * `RANGE`
    * `ARITY`
    * `NOTSUPPORTED`
  * Clean separation from protocol errors
  * No mutation on operation failure

* **Metrics & INFO**

  * Added per-command counters:

    * `EXISTS`, `SETNX`, `GETSET`
    * `INCR`, `DECR`, `INCRBY`, `DECRBY`
    * `MGET`, `MSET`, `DBSIZE`
  * Added error counters:

    * `op_wrongtype_errors`
    * `op_range_errors`
  * Extended `INFO` (additive only)

**Key Decisions:**

* Storage exposes logical state, not internal entries
* Mutation returns **decision**, storage applies it
* TTL preserved for numeric operations
* Multi-key protocol uses explicit item count
* `DBSIZE` is exact (scan-based)

**Deferred:**

* Transactional semantics for `MSET`
* Lock segmentation tuning (beyond basic striping)
* Optimized keyspace size tracking (non-scan)
* Extended error categories beyond current set

---

This cleanly captures what you actually built—including the important **striped locking upgrade**, which is a meaningful improvement beyond the original plan.

Phase 4 is properly closed now.

---

## Phase 5 — Configuration, Observability, and Admin Surface

### Completed

* **Immutable configuration model**

  * Introduced typed immutable `Config` snapshot
  * Centralized supported keys:

    * `server.port`
    * `server.backlog`
    * `server.maxClients`
    * `socket.soTimeoutMillis`
    * `metrics.enabled`
  * Builder-based construction with fail-fast validation

* **Layered configuration loading**

  * Implemented deterministic precedence:

    * JVM system properties (`-D`) > environment variables > properties file > defaults
  * Default config file: `./config/kiwi.properties`
  * Override via `-Dkiwi.config=/path/to/file`
  * Centralized parsing of raw string values into typed fields
  * Unknown file keys produce warnings
  * Invalid known values fail startup

* **Runtime config wiring**

  * Server bind port and backlog driven by config
  * Admission control (`maxClients`) driven by config
  * Metrics enable/disable flag wired into observability
  * Socket timeout partially wired (temporary hardcoded value kept for testing)

* **Startup configuration visibility**

  * Effective config snapshot printed once at startup
  * Canonical key names and deterministic output

* **Structured logging foundation**

  * Introduced logging abstraction over underlying logger
  * Flat `key=value` format
  * Connection-level context (`conn_id`)
  * Logs for:

    * connection accept
    * connection close
    * connection refused
    * protocol/parse errors
    * socket timeouts

* **Request context propagation**

  * Introduced `req_id` (monotonic per connection)
  * Propagated `conn_id` + `req_id` across execution pipeline
  * Enabled structured error logging with request context

* **Admin command: `CONFIG GET`**

  * Supports:

    * single key lookup
    * `*` for full config dump
  * Returns effective runtime config (not defaults)
  * Stable output ordering
  * Read-only (no `CONFIG SET`)

* **Extended `INFO`**

  * Added `config` section
  * Includes all Phase 5 config keys
  * Fully derived from immutable config snapshot
  * Preserved existing fields (additive only)

* **Metrics disabled path**

  * `metrics.enabled=false` disables most metric updates
  * Fast-path short-circuiting in observability layer
  * Minimal overhead in hot paths

---

### Known limitations / deferred

* No `CONFIG SET` (runtime mutation not supported)
* No hot reload of configuration
* Socket timeout config not fully wired (temporary testing override present)
* Logging:

  * no log rotation or shipping
  * no advanced filtering/levels
* Metrics:

  * `currentClients` still coupled to metrics/admission logic
* No persistence of config changes

---

### Outcome

* Configuration is now deterministic, validated, and centrally managed
* Runtime behavior is driven by immutable config snapshot
* Observability foundation established (structured logs + metrics toggle)
* Basic admin surface available (`CONFIG GET`, `INFO.config`)
* System is ready for further operational features (replication, clustering, persistence)

---

---

## Phase 6 — Expiry Engine & Memory Management

### Completed

* **Phase 6 configuration surface**

  * Added immutable configuration support for:

    * `ttl.sampler_period_ms`
    * `ttl.sample_batch`
    * `ttl.backoff_max_ms`
    * `memory.max_bytes`
    * `eviction.policy`
  * Validation integrated into bootstrap
  * Added visibility through `CONFIG GET` and `INFO.config`

* **Active expiry engine**

  * Introduced dedicated background expiry sampler thread
  * Integrated sampler into server lifecycle
  * Cooperative startup and shutdown
  * Interrupt-driven shutdown support
  * Runtime-owned component (not embedded into storage)

* **Expiry sampling infrastructure**

  * Added dedicated storage sampling interface
  * Introduced sampling API for TTL-bearing keys
  * Added safe delete path that re-validates expiry under lock before removal
  * Preserved correctness under concurrent writes and expiry races
  * Unified deletion accounting with existing storage semantics

* **Active expiry execution**

  * Sampler periodically scans TTL candidates
  * Expired keys are proactively removed without requiring reads
  * Added metrics:

    * `ttl_scanned`
    * `ttl_active_expired_evictions`
  * Active expiry now complements existing lazy expiry mechanism

* **Adaptive sampler throttling**

  * Implemented dynamic backoff behavior
  * Sampler becomes less aggressive when little work is found
  * Sampler returns to active mode when expired keys are detected
  * Configurable upper bound via `ttl.backoff_max_ms`
  * Reduced unnecessary background CPU activity

* **Approximate memory accounting**

  * Introduced storage-level memory tracking
  * Accounting based on:

    * key size
    * value size
    * fixed entry overhead estimate
  * Memory usage updated on:

    * writes
    * overwrites
    * deletes
    * lazy expiry
    * active expiry
  * Added metrics:

    * `memory_used_bytes`
    * `memory_max_bytes`

* **Memory guard (`noevict`)**

  * Implemented configurable memory cap via `memory.max_bytes`
  * Added deterministic write admission checks
  * Writes exceeding configured limits are rejected
  * Existing data is preserved
  * Added operation-level error:

    * `MEMORY_LIMIT`
  * Added metric:

    * `eviction_triggered`

* **INFO and observability extensions**

  * Added TTL and memory visibility through `INFO`
  * Added active expiry metrics
  * Added memory usage and configured limits
  * Preserved additive-only INFO evolution
  * All new Phase 6 configuration values exposed through admin surfaces

---

### Key Decisions

* Active expiry implemented using a dedicated sampler thread rather than expiry queues or timing wheels
* Sampling results are treated as potentially stale; deletion re-validates expiry under lock
* Expiry uses the same storage deletion/accounting paths as normal operations
* Memory accounting is approximate by design and intended for operational guardrails rather than JVM-accurate measurement
* `memory.max_bytes = 0` disables memory enforcement
* `noevict` chosen as the only supported eviction policy for Phase 6
* Background expiry uses adaptive backoff to avoid competing with foreground workloads

---

### Deferred

* `allkeys-lru` eviction policy
* Alternative eviction policies
* Expiry index structures (heap, timing wheel, bucketed queues)
* Dynamic sampler batch sizing
* Precise JVM-level memory accounting
* Persistence-aware memory management

---

### Notes on LRU Deferral

The original roadmap allowed optional implementation of `allkeys-lru`. During implementation it was intentionally deferred.

Reasoning:

* Kiwi currently operates as an in-memory database rather than a cache.
* There is no persistence layer yet.
* Under sustained load, memory pressure can be reached quickly, making "least recently used" keys only seconds old.
* Automatic removal of arbitrary keys was considered unsafe for current database-style semantics.
* Deterministic rejection of new writes (`noevict`) was preferred over silent loss of existing data.

`allkeys-lru` may be revisited later if Kiwi gains a dedicated cache mode or persistence guarantees.

---

### Outcome

* Expired keys are now removed proactively without requiring reads.
* Memory usage is observable and bounded through configurable limits.
* Memory pressure results in deterministic behavior instead of uncontrolled growth.
* Operational visibility significantly improved through metrics, INFO, and configuration surfaces.
* Phase 6 exit criteria achieved with active expiry, memory guardrails, observability, and runtime tuning controls.

---

**Phase 6 closed.** The system now has proactive expiry, bounded memory behavior, and the operational foundations required before replication work begins.




