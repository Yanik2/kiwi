Short answer: they **must be chosen as a coherent budget**, not three independent knobs. They define:

* how many **reader threads** and socket buffers you can afford (`maxClients`),
* how much **compute throughput** you actually have (pool thread count),
* how much **burst** you’ll absorb before you start **pushing back** (queue capacity).

Let’s unpack this in a way you can translate directly into defaults and comments.

---

## 1. What each knob really controls

### `maxClients`

Upper bound on:

* number of open TCP connections,
* number of per-connection reader threads (in your current design),
* number of per-connection read buffers + connection state structs.

So it mainly hits:

* **memory** (thread stacks, buffers, connection objects),
* **scheduler overhead** (too many runnable threads → context switching).

### Thread pool size (`poolThreads`)

Upper bound on **parallel request execution**:

* Max tasks running at the same time.
* Approximate throughput ≈ `poolThreads * ops_per_thread_per_sec` (for CPU-bound, in-memory handlers).

This is your **CPU budget** knob.

### Queue capacity (`queueCapacity`)

Upper bound on:

* how many **requests are waiting** (not yet executing),
* how bad latency can spike before you hit **backpressure / rejection**.

This is your **burst cushion** and **latency upper bound** knob.

---

## 2. Core correlations (rules of thumb)

Forget exact numbers for a minute; think relations and inequalities.

### 2.1. Pool size vs CPU cores

For your Redis-like, in-memory KV store, handlers are mostly CPU/heap bound.

Rule of thumb:

* `poolThreads ≈ #cores` or `2 * #cores` (if handlers occasionally block on locks or minor stuff).

If `poolThreads >> cores`, you’re just oversubscribing and increasing context switches.
If `poolThreads << cores`, you’re leaving CPU underutilized.

So: **pool size is a function of machine, not of `maxClients`**.

---

### 2.2. Queue capacity vs pool size

You want the queue to:

* be **big enough** to absorb short bursts,
* be **small enough** that:

    * latency doesn’t explode,
    * backpressure/rejection kicks in **before** the system falls over.

Good mental model:

* Let each worker process `R` requests/sec.
* Total throughput: `capacity = poolThreads * R`.

If you want to absorb at most `T` seconds of burst before rejecting, choose:

> `queueCapacity ≈ capacity * T`
> e.g. `T = 0.5…2` seconds is usually enough.

Example:

* 8 worker threads, each ~5k ops/sec → `capacity ≈ 40k req/s`.
* You’re okay with 1 second worth of backlog → `queueCapacity ≈ 40k`.

But that’s a bit large for a first cut. That’s the conceptual formula. Realistically for a toy/educational-but-real KV store you probably want:

> `queueCapacity ≈ poolThreads * K`, with `K` somewhere between 10 and 100.

So:

* `poolThreads = 8`, `K = 50` → `queueCapacity = 400`.
* That’s **bounded**, and you’ll see backpressure very quickly.

Key point:
**Queue capacity should scale with pool size, not with maxClients.**
If you set `queueCapacity` ~ `maxClients`, a burst from many clients will create a huge backlog and insane tail latency before backpressure does anything.

---

### 2.3. maxClients vs thread pool + queue

This is the tricky one, because `maxClients` embeds **two different concerns**:

1. OS-level resource caps (threads, memory, file descriptors).
2. How much unfairness you’ll tolerate between clients.

#### 2.3.1. Resource budgeting

Given your per-connection model (1 reader thread, 1 buffer, some struct), approximate:

* per-thread stack + metadata: `S` bytes (e.g. 256 KiB–1 MiB depending on your `-Xss` and VM).
* per-connection buffer/state: `B` bytes (e.g. 8–64 KiB).

Memory cost ≈ `maxClients * (S + B + conn_struct)`.

You decide an upper bound for “connection overhead” memory, e.g. `M_conn_budget`.

> `maxClients ≤ M_conn_budget / (S + B + overhead)`

So **maxClients is primarily capped by memory**, not by pool size.
Plus OS limits (FDs etc.), but that’s orthogonal.

#### 2.3.2. Fairness / throughput interactions

Even if the box can “technically” handle the threads, there’s a logical relationship:

* Every client’s requests end up in **one shared queue** behind **one shared pool**.
* If `maxClients` is huge relative to `poolThreads` and `queueCapacity`, then:

    * a small subset of aggressive clients can dominate the queue,
    * everyone else sees massive latency or rejections.

At Phase 3, you don’t yet have per-connection quotas. So a practical rule:

> For initial defaults, keep `maxClients` within maybe `10x–50x poolThreads`.

Not a hard law, but a **sanity bound**.
Example:

* `poolThreads = 8`, `maxClients` in the range `80–400` as a starting point.
* You can still spike higher later, but you know what regime you’re in.

If you set:

* `poolThreads = 8`,
* `maxClients = 5000`,
* `queueCapacity = 100`,

then:

* one asshole client with deep pipelining can constantly keep the queue full,
* everyone else gets rejected constantly, even though `maxClients` is “allowed”.

You’ll eventually need more sophisticated mechanisms (per-connection queue caps, fair scheduling) to make very high `maxClients` meaningful. For Phase 3, keep `maxClients` **reasonable relative to poolThreads**.

---

## 3. How they shape overload behavior

Think about the overload pipeline:

1. **Clients** send requests → per-connection reader threads parse frames.
2. Readers **submit tasks** into the **bounded queue**.
3. Worker threads **drain** the queue at throughput ≈ `poolThreads * R`.

Now walk through what happens as load increases:

### 3.1. Below capacity

* Queue mostly empty: `tp_queue_size` fluctuates around 0.
* `tp_workers_active` ≤ `poolThreads`, often < `poolThreads`.
* `tp_rejected = 0`.
* `maxClients` is irrelevant except for resource usage.

### 3.2. Around capacity

* `tp_workers_active` is usually = `poolThreads`.
* Queue depth fluctuates between 0 and some small number.
* `tp_rejected` still 0; latency stays reasonable.

Here, **queueCapacity** defines how jitter-tolerant you are.

### 3.3. Above capacity (sustained overload)

* Queue fills to `queueCapacity`.
* New submissions:

    * either get **rejected** (increment `tp_rejected`),
    * or run in caller (if `CALLER_RUNS`, shifting pain back to reader threads).

Here, if:

* `maxClients` is huge,
* and one subset of clients is much more aggressive,

they will occupy the queue constantly. That’s not fixable by tuning alone; but smaller `maxClients` reduces absolute worst-case damage.

**Key invariant:**
System must **enter overload in a controlled way**:

* bounded queue,
* visible rejections/backpressure,
* no OOM, no thread explosion.

The correlation ensures exactly that:

* `queueCapacity` is not insane,
* `poolThreads` matches hardware,
* `maxClients` doesn’t destroy you with thread/memory overhead.

---

## 4. Reasonable first defaults (as a starting template)

Assume:

* 8-core machine,
* in-memory KV, CPU-bound-ish operations,
* you care about low-ish latency.

You might start with:

* `poolThreads = cores` or `2 * cores` → `8–16`.
* `queueCapacity = poolThreads * 32` → `256–512`.
* `maxClients = poolThreads * 20` → `160–320`.

Then you load-test:

* if CPU is idle but `tp_queue_size` is high → your `poolThreads` is too small (increase).
* if latency is huge but `tp_rejected` is 0 and queue is large → `queueCapacity` is too big (reduce).
* if the OS is dying from too many threads or memory → reduce `maxClients` or shrink stacks/buffers.

These relationships should go into comments near your config parsing, as **design notes**, not just random numbers.

---

## 5. How to explain this in your own docs

When you later write “real” documentation, you want a short, blunt description like:

* `maxClients` — hard cap on concurrent connections. Raising this increases memory and OS thread usage (one reader thread + buffer per client). Does **not** increase throughput without changing pool size.
* `executor.workers` — number of worker threads executing commands. Throughput is roughly proportional to this, up to the number of CPU cores.
* `executor.queue_capacity` — maximum number of queued commands waiting for workers. Higher values smooth short bursts but increase tail latency. When full, new commands are rejected or executed on the caller, depending on policy.

And a single line tying them together:

> Choose `executor.workers` based on CPU cores, `executor.queue_capacity` as a small multiple of workers, and `maxClients` based on memory/OS limits and fairness expectations; don’t scale queue capacity or throughput directly with `maxClients`.

---

If you want, next step we can design **explicit formulas & comments** you’ll embed right next to your config parsing so future-you remembers the rationale. No code, just the literal text you’d put there.
