package com.kiwi.server.response;

import java.util.logging.Logger;

public final class TtlResponse implements SerializableValue {
    private static final Logger logger = Logger.getLogger(TtlResponse.class.getName());

    private final long ttl;

    public TtlResponse(long ttl) {
        this.ttl = ttl;
    }

    @Override
    public byte[] serialize() {
        if (ttl != -1 && ttl != -2) {
            if (ttl < -2) {
                return new byte[]{48};
            } else {
                return serializeLong(ttl);
            }
        } else {
            return new byte[]{45, (byte) (-ttl + 48)};
        }
    }

    private byte[] serializeLong(long value) {
        logger.info("Serializing long value: [" + value + "]");

        int arraySize = 19;
        long divider = 10_000_000_000_000_000_00L;

        while (divider > value) {
            divider /= 10;
            arraySize--;
        }

        final byte[] result = new byte[arraySize];
        int index = 0;

        while (divider > 0) {
            final long b = value / divider;
            result[index] = (byte) (b + 48);
            value -= (divider * b);
            divider /= 10;
            index++;
        }

        return result;
    }
}
