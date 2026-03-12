package com.kiwi.persistent.factory;

import com.kiwi.observability.factory.ObservabilityContainer;
import com.kiwi.persistent.Storage;

public class PersistentModule {

    public static PersistentContainer create(ObservabilityContainer observabilityContainer) {
        return new PersistentContainer(new Storage(observabilityContainer.storageMetrics()));
    }
}
