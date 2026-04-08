package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.StorageFacade;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.TCPRequest;

public class DbSizeCommandHandler extends StorageCommandHandler {
    public DbSizeCommandHandler(StorageFacade storageFacade) {
        super(storageFacade);
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var storageSize = storageFacade.size();
        return new OperationResult(() -> {
            final byte[] result = new byte[4];
            result[0] = (byte) (storageSize >> 24);
            result[1] = (byte) (storageSize >> 16);
            result[2] = (byte) (storageSize >> 8);
            result[3] = (byte) (storageSize);
            return result;
        }, true);
    }
}
