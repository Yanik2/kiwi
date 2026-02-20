package com.kiwi.server.dto;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.server.parsing.ParsingStatus;

public record ParserResult<T>(ParsingStatus status, T value, ProtocolException error) {
    public ParserResult(ParsingStatus status, ProtocolException error) {
        this(status, null, error);
    }

    public ParserResult(ParsingStatus status) {
        this(status, null, null);
    }

    public ParserResult(ParsingStatus status, T value) {
        this(status, value, null);
    }
}
