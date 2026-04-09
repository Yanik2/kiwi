package com.kiwi.server.dispatcher.command;

import com.kiwi.observability.OperationErrorMetrics;
import com.kiwi.persistent.StorageFacade;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.model.expiration.NoOpExpiration;
import com.kiwi.persistent.mutation.MutationDecision;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.NumericRequest;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.DataResponse;

import static com.kiwi.persistent.mutation.ErrorType.RANGE;
import static com.kiwi.persistent.mutation.ErrorType.WRONG_TYPE;
import static com.kiwi.server.request.Method.DECR;
import static com.kiwi.server.request.Method.DECRBY;

public class NumericOperationCommandHandler extends StorageCommandHandler {
    private final OperationErrorMetrics operationErrorMetrics;

    public NumericOperationCommandHandler(StorageFacade storageFacade, OperationErrorMetrics errorMetrics) {
        super(storageFacade);
        this.operationErrorMetrics = errorMetrics;
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var numericRequest = (NumericRequest) request;
        final long addValue = DECR.equals(numericRequest.getMethod()) || DECRBY.equals(numericRequest.getMethod())
                ? -numericRequest.getValue()
                : numericRequest.getValue();

        final var mutationResult = storageFacade.mutate(new Key(numericRequest.getKey()), state -> {
            long value = 0;
            if (state.exists()) {
                final var parseResult = parseValue(state.value().getValue());
                if (!parseResult.success) {
                    operationErrorMetrics.onError(WRONG_TYPE);
                    return new MutationDecision.Error(WRONG_TYPE);
                }
                value = parseResult.value;
            }

            if (value > 0 && addValue > 0 && (Long.MAX_VALUE - value) < addValue) {
                operationErrorMetrics.onError(RANGE);
                return new MutationDecision.Error(RANGE);
            }

            if (value < 0 && addValue < 0 && (Long.MIN_VALUE - value) > addValue) {
                operationErrorMetrics.onError(RANGE);
                return new MutationDecision.Error(RANGE);
            }

            final var result = convertToBytes(value + addValue);
            final var newValue = new Value(result, state.exists()
                            ? state.value().getExpiryPolicy()
                            : NoOpExpiration.getInstance()
            );
            return new MutationDecision.Write(true, newValue, newValue);
        });

        return new OperationResult(
                new DataResponse(mutationResult.value().orElseGet(() -> new Value(new byte[0]))),
                mutationResult.success()
        );
    }

    private byte[] convertToBytes(long value) {
        final var buf = new byte[19];
        final boolean isNegative = value < 0;
        int counter = isNegative ? 1 : 0;
        int index = 19;

        do {
            byte tmp = (byte) (value % 10);
            buf[--index] = tmp < 0 ? (byte)-tmp : tmp;
            counter++;
            value /= 10;
        } while (value != 0);

        final var result = new byte[counter];
        int startIndex = 0;
        if (isNegative) {
            result[0] = '-';
            startIndex = 1;
        }
        for (int i = startIndex, j = 0; i < counter; i++, j++) {
            result[i] = (byte) (buf[index + j] + 48);
        }

        return result;
    }

    private ParseResult parseValue(byte[] byteValue) {
        final var maxLength = byteValue[0] == 45 ? 20 : 19;
        if (byteValue.length > maxLength) {
            return new ParseResult(false, 0);
        }

        final boolean isNegative = byteValue[0] == 45;
        if (isNegative && byteValue.length < 2) {
            return new ParseResult(false, 0);
        }
        int index = isNegative ? 1 : 0;

        long result = 0;
        for (; index < byteValue.length; index++) {
            result *= 10;
            final byte delta = (byte) (byteValue[index] - 48);
            if (delta < 0 || delta > 9) {
                return new ParseResult(false, 0);
            }
            if ((result + delta) < result) {
                return new ParseResult(false, 0);
            }
            result += delta - 48;
        }

        return new ParseResult(true, isNegative ? -result : result);
    }

    private record ParseResult(
            boolean success,
            long value
    ) {
    }
}
