package com.kiwi.persistent.factory;

import com.kiwi.config.domain.Config;
import com.kiwi.observability.factory.ObservabilityContainer;
import com.kiwi.persistent.storage.StorageStrippingLockImpl;

public class PersistentModule {

    public static PersistentContainer create(ObservabilityContainer observabilityContainer, Config config) {
        return new PersistentContainer(
                new StorageStrippingLockImpl(
                        observabilityContainer.storageMetrics(),
                        config.memoryMaxBytes()
                )
        );
    }
}
