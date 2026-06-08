package com.kiwi.persistent.storage;

import com.kiwi.persistent.model.Key;

import java.util.List;

public interface ExpirySamplingStorage {
    List<Key> sampleKeysWithTtl(int limit);

    boolean deleteIfExpired(Key key, long millisNow);
}
