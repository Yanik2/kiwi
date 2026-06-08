package com.kiwi.persistent.storage;

import com.kiwi.persistent.model.Key;

import java.util.List;

public interface SampleStorage {
    List<Key> getExpiryKeys(int limit);

    boolean deleteExpiryKey(Key key, long millisNow);
}
