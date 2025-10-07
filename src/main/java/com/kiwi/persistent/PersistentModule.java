package com.kiwi.persistent;

import com.kiwi.observability.ObservabilityModule;

public class PersistentModule {

    public static Storage create() {
        return new Storage(ObservabilityModule.getStorageMetrics());
    }

}
