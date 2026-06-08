package com.kiwi.persistent.factory;

import com.kiwi.persistent.storage.StorageStrippingLockImpl;

public record PersistentContainer(
        StorageStrippingLockImpl storageFacade
) {
}
