package com.kiwi.observability.metrics;

import com.kiwi.exception.protocol.ProtocolErrorCode;

public interface RequestMetrics {
    void onAccept();
    void onDrainTimeout();
    void onPendingResponse(int delta);
    void onConnection();
    void onReaderThreadActive(int delta);
    void onClose();
    void onParse(long bytes);
    void onWrite(long bytes);
    void onProtoError(ProtocolErrorCode protocolErrorCode);
    void onRefuse();
    long getCurrentClients();
}
