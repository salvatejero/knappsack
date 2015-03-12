package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.StorageConfiguration;
import com.sparc.knappsack.forms.StorageForm;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface StorageConfigurationService extends EntityService<StorageConfiguration> {

    <T extends StorageConfiguration> T get(Long id, Class<T> storageConfigurationClass);

    void update(StorageForm storageForm);

    StorageConfiguration getByName(String name);

    List<StorageConfiguration> getAll();

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    StorageConfiguration createStorageConfiguration(StorageForm storageForm);

    StorageConfiguration getRegistrationDefault();
}
