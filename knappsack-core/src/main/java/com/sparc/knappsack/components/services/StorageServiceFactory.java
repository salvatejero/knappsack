package com.sparc.knappsack.components.services;

import com.sparc.knappsack.enums.StorageType;

public interface StorageServiceFactory {
    StorageService getStorageService(StorageType storageType);
}
