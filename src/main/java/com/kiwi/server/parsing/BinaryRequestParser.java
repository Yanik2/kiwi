package com.kiwi.server.parsing;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.server.buffer.Cursor;
import com.kiwi.server.request.Method;
import com.kiwi.server.request.model.DefaultRequest;

import java.util.LinkedList;
import java.util.List;

import static com.kiwi.exception.protocol.ProtocolErrorCode.INVALID_HEADER;
import static com.kiwi.exception.protocol.ProtocolErrorCode.INVALID_SEPARATOR;
import static com.kiwi.exception.protocol.ProtocolErrorCode.UNKNOWN_METHOD;
import static com.kiwi.server.parsing.ParsingStatus.ERROR;
import static com.kiwi.server.parsing.ParsingStatus.NEED_MORE_DATA;
import static com.kiwi.server.parsing.ParsingStatus.OK;
import static com.kiwi.server.util.ServerConstants.SEPARATOR;

public class BinaryRequestParser {

    private static final int KEY_HEADER_LEN = 2;
    private static final int MULTIKEYS_HEADER_LEN = 2;
    private static final int VALUE_HEADER_LEN = 4;
    private static final int MAX_KEY_LENGTH = 4096;
    private static final int MAX_VALUE_LENGTH = 10485760;

    public List<ParserResult<ParsedData>> parse(Cursor cursor) {
        final var results = new LinkedList<ParserResult<ParsedData>>();

        while (cursor.bytesAvailable() > 0) {
            final var parsedRequest = parseRequest(cursor);
            results.add(parsedRequest);
            if (ERROR == parsedRequest.status()) {
                break;
            }
        }

        return results;
    }

    private ParserResult<ParsedData> parseRequest(Cursor cursor) {
        final var bytesAvailable = cursor.bytesAvailable();

        if (bytesAvailable < 12) {
            cursor.toEnd();
            return new ParserResult<>(NEED_MORE_DATA);
        }

        final var flags = cursor.pop();
        final var parsedMethod = getMethod(cursor.pop());
        if (ERROR == parsedMethod.status()) {
            return new ParserResult<>(ERROR, parsedMethod.error());
        }

        final var method = parsedMethod.value();
        final var multikeysSize = getHeaderLength(cursor, MULTIKEYS_HEADER_LEN);

        final var keyValuePairs = new LinkedList<DefaultRequest.KeyValuePair>();
        for (int i = 0; i < multikeysSize; i++) {
            if (cursor.bytesAvailable() < 6) {
                cursor.toEnd();
                return new ParserResult<>(NEED_MORE_DATA);
            }

            final var keyLength = getHeaderLength(cursor, KEY_HEADER_LEN);
            final var valueLength = getHeaderLength(cursor, VALUE_HEADER_LEN);
            if (headerNotValid(keyLength, MAX_KEY_LENGTH) || headerNotValid(valueLength, MAX_VALUE_LENGTH)) {
                return new ParserResult<>(ERROR, new ProtocolException("Header is invalid", INVALID_HEADER));
            }

            if ((cursor.bytesAvailable()) < keyLength + valueLength) {
                cursor.toEnd();
                return new ParserResult<>(NEED_MORE_DATA);
            }

            final var key = cursor.getBytes(new byte[keyLength], keyLength);
            final var value = cursor.getBytes(new byte[valueLength], valueLength);
            keyValuePairs.add(new DefaultRequest.KeyValuePair(key, value));
        }

        return validateSeparatorAndReturn(cursor, flags, method, keyValuePairs);
    }

    private int getHeaderLength(Cursor cursor, int headerSize) {
        int len = 0;
        for (int i = 0; i < headerSize; i++) {
            final byte b = cursor.pop();
            len = len << 8;
            len = len | (b & 255);
        }

        return len;
    }

    private ParserResult<Method> getMethod(int methodId) {
        final var methods = Method.values();
        if (methodId >= 0 && methodId < methods.length) {
            return new ParserResult<>(OK, methods[methodId]);
        } else {
            return new ParserResult<>(ERROR, new ProtocolException("Invalid method id: " + methodId, UNKNOWN_METHOD));
        }
    }

    private ParserResult<ParsedData> validateSeparatorAndReturn(Cursor cursor,
                                                                byte flags,
                                                                Method method,
                                                                List<DefaultRequest.KeyValuePair> keyValuePairs) {
        if (cursor.bytesAvailable() < 2) {
            cursor.toEnd();
            return new ParserResult<>(NEED_MORE_DATA);
        }
        final var firstByte = cursor.pop();
        final var secondByte = cursor.pop();
        if (SEPARATOR[0] == firstByte && SEPARATOR[1] == secondByte) {
            cursor.advance();
            return new ParserResult<>(OK, new ParsedData(flags, method, keyValuePairs));
        } else {
            return new ParserResult<>(ERROR, new ProtocolException("Separator does not validate", INVALID_SEPARATOR));
        }
    }

    private boolean headerNotValid(int header, int maxValue) {
        return header < 0 || header > maxValue;
    }
}
