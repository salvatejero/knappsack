package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.KeyVaultEntry;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.forms.KeyVaultEntryForm;

public interface KeyVaultService<T extends KeyVaultEntry>  {

    /**
     * Create a new KeyVaultEntry entity
     *
     * @param keyVaultEntryForm Form object
     * @return newly created entity
     */
    T createKeyVaultEntry(KeyVaultEntryForm keyVaultEntryForm);

    boolean resign(T keyVaultEntry, ApplicationVersion applicationVersion, final AppState requestedAppState);

    void deleteFiles(T keyVaultEntry);

}
