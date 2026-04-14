package com.kiwi.persistent.factory;

import com.kiwi.persistent.storage.Storage;

public record PersistentContainer(
        Storage storageFacade
) {
}
