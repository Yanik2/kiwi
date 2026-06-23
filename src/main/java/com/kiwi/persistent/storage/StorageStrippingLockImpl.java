package com.kiwi.persistent.storage;

import com.kiwi.observability.metrics.StorageMetrics;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.mutation.CurrentState;
import com.kiwi.persistent.mutation.Mutation;
import com.kiwi.persistent.mutation.MutationDecision;
import com.kiwi.persistent.result.MutationResult;
import com.kiwi.persistent.result.WriteResult;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.kiwi.persistent.mutation.ErrorType.MEMORY_LIMIT;

public class StorageStrippingLockImpl implements Storage, ExpirySamplingStorage {
    private static final Value EMPTY_VALUE = new Value(new byte[0]);
    private static final int ENTRY_OVERHEAD_BYTES = 64;

    private final StorageMetrics storageMetrics;
    private final int memoryMaxBytes;

    private final Map<Key, Value> inMemoryStorage = new HashMap<>();
    private volatile ReentrantLock[] locks;
    private final ReentrantLock generalLock = new ReentrantLock();
    private final ReentrantLock memoryBytesLock = new ReentrantLock();

    private volatile boolean resizeInProgress = false;

    public StorageStrippingLockImpl(StorageMetrics storageMetrics, int memoryMaxBytes) {
        this.storageMetrics = storageMetrics;
        this.memoryMaxBytes = memoryMaxBytes;
        this.locks = new ReentrantLock[16];
        for (int i = 0; i < 16; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    @Override
    public Optional<Value> read(Key key) {
        while (resizeInProgress || generalLock.isLocked()) {}
        final var snapLocks = locks;
        final var lock = snapLocks[Math.abs(key.hashCode() % snapLocks.length)];
        lock.lock();
        try {
            return expirationGate(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public WriteResult write(Key key, Value value) {
        while (resizeInProgress || generalLock.isLocked()) {
        }
        resizeLocks();
        final var lock = locks[Math.abs(key.hashCode() % locks.length)];
        lock.lock();
        try {
            final var delta = inMemoryStorage.containsKey(key)
                    ? value.size() - inMemoryStorage.get(key).size()
                    : key.size() + value.size() + ENTRY_OVERHEAD_BYTES;
            if (exceedsMaxCap(delta)) {
                return new WriteResult(false, new Value(MEMORY_LIMIT.name().getBytes()));
            } else {
                inMemoryStorage.put(key, value);
                if (value.getExpiryPolicy().hasTtl()) {
                    storageMetrics.onKeyWithExpiration(1);
                }
                return new WriteResult(true, EMPTY_VALUE);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public MutationResult mutate(Key key, Mutation mutation) {
        while (resizeInProgress || generalLock.isLocked()) {
        }
        resizeLocks();
        final var lock = locks[Math.abs(key.hashCode() % locks.length)];
        lock.lock();

        try {
            final var value = expirationGate(key);
            final var state = new CurrentState(value.isPresent(), value.orElse(null));
            final var mutationDecision = mutation.apply(state);
            return switch (mutationDecision) {
                case MutationDecision.Write w -> {
                    final int delta = value.map(v -> w.value().size() - v.size())
                                    .orElseGet(() -> key.size() + w.value().size() + ENTRY_OVERHEAD_BYTES);
                    if (exceedsMaxCap(delta)) {
                        yield new MutationResult(key, Optional.of(new Value(MEMORY_LIMIT.name().getBytes())), false);
                    } else {
                        inMemoryStorage.put(key, w.value());
                        if (value.isEmpty()) {
                            if (w.value().getExpiryPolicy().hasTtl()) {
                                storageMetrics.onKeyWithExpiration(1);
                            }
                        } else {
                            final var oldValue = value.get();
                            final var expirationDelta = oldValue.getExpiryPolicy().hasTtl()
                                    ? w.value().getExpiryPolicy().hasTtl() ? 0 : -1
                                    : w.value().getExpiryPolicy().hasTtl() ? 1 : 0;
                            storageMetrics.onKeyWithExpiration(expirationDelta);
                        }
                        yield new MutationResult(key, Optional.ofNullable(w.returnValue()), w.success());
                    }
                }
                case MutationDecision.Delete d -> {
                    final var delta =
                            value.map(v -> -(v.size() + key.size() + ENTRY_OVERHEAD_BYTES)).orElse(0);
                    storageMetrics.onMemoryBytes(delta);
                    inMemoryStorage.remove(key);
                    yield new MutationResult(key, Optional.empty(), d.success());
                }
                case MutationDecision.NoOp n -> new MutationResult(key, Optional.empty(), n.success());
                case MutationDecision.Error e -> new MutationResult(key, Optional.of(
                        new Value(e.errorType().name().getBytes())
                ), false);
            };
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void delete(Key key) {
        while (resizeInProgress || generalLock.isLocked()) {
        }
        resizeLocks();
        final var lock = locks[Math.abs(key.hashCode() % locks.length)];
        Value value;
        lock.lock();
        try {
            value = inMemoryStorage.get(key);
            if (value != null && value.getExpiryPolicy().shouldEvictOnRead(System.currentTimeMillis())) {
                final var delta = -(key.size() + value.size() + ENTRY_OVERHEAD_BYTES);
                storageMetrics.onMemoryBytes(delta);
                storageMetrics.onTtlExpiredEviction();
                storageMetrics.onKeyWithExpiration(-1);
            }
            inMemoryStorage.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        generalLock.lock();
        try {
            int size = inMemoryStorage.size();
            final var keyList = new LinkedList<>(inMemoryStorage.keySet());
            for (Key k : keyList) {
                if (expirationGate(k).isEmpty()) {
                    size--;
                }
            }

            return size;
        } finally {
            generalLock.unlock();
        }
    }

    @Override
    public List<Key> sampleKeysWithTtl(int limit) {
        generalLock.lock();
        try {
            return inMemoryStorage.entrySet().stream()
                    .filter(e -> e.getValue().getExpiryPolicy().hasTtl())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .toList();
        } finally {
            generalLock.unlock();
        }
    }

    @Override
    public boolean deleteIfExpired(Key key, long millisNow) {
        while (resizeInProgress || generalLock.isLocked()) {
        }
        resizeLocks();
        final var lock = locks[Math.abs(key.hashCode() % locks.length)];
        lock.lock();
        try {
            final var value = inMemoryStorage.get(key);
            if (value == null || !value.getExpiryPolicy().shouldEvictOnRead(millisNow)) {
                return false;
            } else {
                final var delta = -(key.size() + value.size() + ENTRY_OVERHEAD_BYTES);
                storageMetrics.onMemoryBytes(delta);
                inMemoryStorage.remove(key);
                if (value.getExpiryPolicy().hasTtl()) {
                    storageMetrics.onKeyWithExpiration(-1);
                }
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    private void resizeLocks() {
        if (inMemoryStorage.size() / 4 < locks.length && inMemoryStorage.size() > locks.length) {
            return;
        }

        if (generalLock.tryLock()) {
            try {
                this.resizeInProgress = true;
                final var newLocks = new ReentrantLock[Math.max(inMemoryStorage.size() / 2, 16)];
                final var oldLocks = this.locks;
                for (Lock l : oldLocks) {
                    l.lock();
                }

                for (int i = 0; i < newLocks.length; i++) {
                    newLocks[i] = new ReentrantLock();
                }

                this.locks = newLocks;
                this.resizeInProgress = false;

                for (Lock l : oldLocks) {
                    l.unlock();
                }
            } finally {
                generalLock.unlock();
            }
        }
    }

    private Optional<Value> expirationGate(Key key) {
        final var value = inMemoryStorage.get(key);
        if (value == null) {
            return Optional.empty();
        }

        if (value.getExpiryPolicy().shouldEvictOnRead(System.currentTimeMillis())) {
            inMemoryStorage.remove(key);
            storageMetrics.onTtlExpiredEviction();
            storageMetrics.onMemoryBytes(-(key.size() + value.size() + ENTRY_OVERHEAD_BYTES));
            storageMetrics.onKeyWithExpiration(-1);
            return Optional.empty();
        }

        return Optional.of(value);
    }

    private boolean exceedsMaxCap(int delta) {
        memoryBytesLock.lock();
        try {
            if (memoryMaxBytes == 0 || storageMetrics.getMemoryUsedBytes() + delta <= memoryMaxBytes) {
                storageMetrics.onMemoryBytes(delta);
                return false;
            } else {
                storageMetrics.onEvictionTriggered();
                return true;
            }
        } finally {
            memoryBytesLock.unlock();
        }

    }

}
