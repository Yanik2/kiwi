package com.kiwi.persistent.model.expiration;

public interface ExpiryPolicy {
    boolean shouldEvictOnRead(long currentTime);
    long remainingTime(long currentTime);
    boolean hasTtl();
}
