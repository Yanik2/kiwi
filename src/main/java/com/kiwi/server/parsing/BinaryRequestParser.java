package com.kiwi.server.parsing;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.server.Method;
import com.kiwi.server.buffer.Cursor;
import com.kiwi.server.dto.ParsedRequest;
import com.kiwi.server.dto.ParserResult;

import java.util.LinkedList;
import java.util.List;

import static com.kiwi.exception.protocol.ProtocolErrorCode.*;
import static com.kiwi.server.parsing.ParsingStatus.*;
import static com.kiwi.server.util.ServerConstants.SEPARATOR;

public class BinaryRequestParser {

    private static final int KEY_HEADER_LEN = 2;
    private static final int VALUE_HEADER_LEN = 4;
    private static final int MAX_KEY_LENGTH = 4096;
    private static final int MAX_VALUE_LENGTH = 10485760;

    public List<ParserResult<ParsedRequest>> parse2(Cursor cursor) {
        final var results = new LinkedList<ParserResult<ParsedRequest>>();

        while (cursor.bytesAvailable() > 0) {
            results.add(parse(cursor));
        }

        return results;
    }

    public ParserResult<ParsedRequest> parse(Cursor cursor) {
        final var bytesAvailable = cursor.bytesAvailable();

        if (bytesAvailable < 10) {
            cursor.toEnd();
            return new ParserResult<>(ParsingStatus.NEED_MORE_DATA);
        }

        final var flags = cursor.pop();
        final var parsedMethod = getMethod(cursor.pop());
        if (ERROR == parsedMethod.status()) {
            return new ParserResult<>(ERROR, parsedMethod.error());
        }

        final var method = parsedMethod.value();
        final var keyLength = getHeaderLength(cursor, KEY_HEADER_LEN);
        final var valueLength = getHeaderLength(cursor, VALUE_HEADER_LEN);
        if (!isHeaderValid(keyLength, MAX_KEY_LENGTH) || !isHeaderValid(valueLength, MAX_VALUE_LENGTH)) {
            return new ParserResult<>(ERROR, new ProtocolException("Header is invalid", INVALID_HEADER));
        }

        if (method.isKeyless()) {
            return validateSeparatorAndReturn(cursor, new ParsedRequest(flags, method));
        }

        if ((bytesAvailable - 8) < keyLength + valueLength + 2) {
            return new ParserResult<>(NEED_MORE_DATA);
        }

        final var key = cursor.getBytes(new byte[keyLength], keyLength);
        final var value = cursor.getBytes(new byte[valueLength], valueLength);
        return validateSeparatorAndReturn(cursor, new ParsedRequest(flags, method, key, value));
    }

    private int getHeaderLength(Cursor cursor, int headerSize) {
        int len = 0;
        for (int i = 0; i < headerSize; i++) {
            final byte b = cursor.pop();
            len = len << 8;
            len = len | b;
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

    private ParserResult<ParsedRequest> validateSeparatorAndReturn(Cursor cursor, ParsedRequest request) {
        final var firstByte = cursor.pop();
        final var secondByte = cursor.pop();
        if (SEPARATOR[0] == firstByte && SEPARATOR[1] == secondByte) {
            cursor.advance();
            return new ParserResult<>(OK, request);
        } else {
            return new ParserResult<>(ERROR, new ProtocolException("Separator does not validate", INVALID_SEPARATOR));
        }
    }

    private boolean isHeaderValid(int header, int maxValue) {
        return header >= 0 && header <= maxValue;
    }
}
