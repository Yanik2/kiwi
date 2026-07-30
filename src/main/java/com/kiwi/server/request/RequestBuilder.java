package com.kiwi.server.request;

import com.kiwi.jvm.factory.JfrEventFactory;
import com.kiwi.jvm.jfr.event.KiwiRequest;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.parsing.ParsedData;
import com.kiwi.server.request.model.DefaultRequest;
import com.kiwi.server.request.model.TCPRequest;

public class RequestBuilder {
    private final JfrEventFactory jfrEventFactory;

    public RequestBuilder(JfrEventFactory jfrEventFactory) {
        this.jfrEventFactory = jfrEventFactory;
    }

    public TCPRequest build(ParsedData parsedData, ConnectionContext context) {
        final var jfrEvent = jfrEventFactory.createEvent(KiwiRequest.class);
        final var request = new DefaultRequest(
                context.getRequestId(),
                jfrEvent,
                parsedData.flags(),
                parsedData.method(),
                parsedData.keyValuePairs()
        );
        jfrEvent.waitForExecution();
        return request;
    }
}
