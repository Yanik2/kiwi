package com.kiwi.persistent.model.expiration;

public class HasTtlExpiration implements ExpiryPolicy {
    private final long expirationTime;

    public HasTtlExpiration(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    @Override
    public boolean shouldEvictOnRead(long currentTime) {
        return expirationTime - currentTime <= 0;
    }

    @Override
    public long remainingTime(long currentTime) {
        return expirationTime - currentTime;
    }

    @Override
    public boolean hasTtl() {
        return true;
    }
}
