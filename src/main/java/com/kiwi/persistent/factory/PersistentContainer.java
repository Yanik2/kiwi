package com.kiwi.persistent.factory;

import com.kiwi.persistent.StorageFacade;

public record PersistentContainer(
        StorageFacade storageFacade
) {
}
