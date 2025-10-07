package com.kiwi.persistent.model.expiration;

public class NoOpExpiration implements ExpiryPolicy {
    private static final ExpiryPolicy instance = new NoOpExpiration();

    public static ExpiryPolicy getInstance() {
        return instance;
    }

    @Override
    public boolean shouldEvictOnRead(long currentTime) {
        return false;
    }

    @Override
    public long remainingTime(long currentTime) {
        return -1;
    }

    @Override
    public boolean hasTtl() {
        return false;
    }
}
