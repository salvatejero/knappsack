package com.sparc.knappsack.components.services;

import com.sparc.knappsack.enums.ApplicationType;

public interface KeyVaultServiceFactory {
    KeyVaultService getKeyVaultService(ApplicationType applicationType);
}
