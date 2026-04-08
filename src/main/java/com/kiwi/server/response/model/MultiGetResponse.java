package com.kiwi.server.response.model;

import com.kiwi.persistent.model.Value;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.kiwi.server.response.ResponseValueConstants.EMPTY_RESPONSE;
import static com.kiwi.server.util.ServerConstants.SEPARATOR;

public record MultiGetResponse(
        List<Optional<Value>> values
) implements SerializableValue {

    @Override
    public byte[] serialize() {
        final var baos = new ByteArrayOutputStream();

        for (Optional<Value> value : values) {
            try {
                baos.write(value.map(Value::getValue).orElse(EMPTY_RESPONSE));
                baos.write(SEPARATOR);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return baos.toByteArray();
    }
}
