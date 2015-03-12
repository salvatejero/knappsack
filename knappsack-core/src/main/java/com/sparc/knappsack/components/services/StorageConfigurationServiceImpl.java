package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.StorageConfigurationDao;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.StorageConfiguration;
import com.sparc.knappsack.forms.StorageForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional( propagation = Propagation.REQUIRED )
@Service("storageConfigurationService")
public class StorageConfigurationServiceImpl implements StorageConfigurationService {

    @Qualifier("storageConfigurationDao")
    @Autowired(required = true)
    private StorageConfigurationDao storageConfigurationDao;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("storageServiceFactory")
    @Autowired
    private StorageServiceFactory storageServiceFactory;



    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void add(StorageConfiguration storageConfiguration) {
        if (storageConfiguration != null) {
            if (storageConfiguration.isRegistrationDefault()) {
                StorageConfiguration currentDefault = storageConfigurationDao.getRegistrationDefault();
                if (currentDefault != null) {
                    currentDefault.setRegistrationDefault(false);
                    update(currentDefault);
                }
            }

            storageConfigurationDao.add(storageConfiguration);
        }
    }

    @Override
    public StorageConfiguration get(Long id) {
        StorageConfiguration storageConfiguration = null;
        if (id != null && id > 0) {
            storageConfiguration = storageConfigurationDao.get(id);
        }
        return storageConfiguration;
    }

    @Override
    public <T extends StorageConfiguration> T get(Long id, Class<T> storageConfigurationClass) {
        if (id == null || id <= 0 || storageConfigurationClass == null) {
            return null;
        }

        return storageConfigurationDao.get(id, storageConfigurationClass);
    }

    @Override
    public StorageConfiguration getRegistrationDefault() {
        return storageConfigurationDao.getRegistrationDefault();
    }

    @Override
    public StorageConfiguration getByName(String name) {
        StorageConfiguration storageConfiguration = null;
        if (name != null && !"".equals(name.trim())) {
            storageConfiguration = storageConfigurationDao.get(name);
        }
        return storageConfiguration;
    }

    @Override
    public List<StorageConfiguration> getAll() {
        return storageConfigurationDao.getAll();
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(Long id) {
        StorageConfiguration storageConfiguration = get(id);

        if (storageConfiguration != null) {
            List<Organization> organizations = organizationService.getAll();
            for (Organization organization : organizations) {
                List<StorageConfiguration> storageConfigurations = organization.getOrgStorageConfig().getStorageConfigurations();
                for (StorageConfiguration configuration : storageConfigurations) {
                    if(configuration.getId().equals(id)) {
                        //Don't delete the storage configuration
                        return;
                    }
                }
            }
            storageConfigurationDao.delete(storageConfiguration);
        }
    }

    @Override
    public void update(StorageConfiguration storageConfiguration) {
        if (storageConfiguration != null && storageConfiguration.getId() != null && storageConfiguration.getId() > 0) {
            storageConfigurationDao.update(storageConfiguration);
        }
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void update(StorageForm storageForm) {
        if (storageForm != null) {
            StorageConfiguration storageConfiguration = get(storageForm.getId());

            if (storageConfiguration != null) {
                if (storageForm.isRegistrationDefault()) {
                    StorageConfiguration currentDefault = storageConfigurationDao.getRegistrationDefault();
                    if (currentDefault != null && !currentDefault.equals(storageConfiguration)) {
                        currentDefault.setRegistrationDefault(false);
                        update(currentDefault);
                    }
                }

                StorageService storageService = storageServiceFactory.getStorageService(storageConfiguration.getStorageType());
                storageService.mapFormToEntity(storageForm, storageConfiguration);

                update(storageConfiguration);
            }
        }
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public StorageConfiguration createStorageConfiguration(StorageForm storageForm) {
        StorageConfiguration storageConfiguration = null;
        if (storageForm != null) {
            storageConfiguration = toStorageConfiguration(storageForm);
            add(storageConfiguration);
        }
        return storageConfiguration;
    }

    private StorageConfiguration toStorageConfiguration(StorageForm storageForm) {
        StorageService storageService = storageServiceFactory.getStorageService(storageForm.getStorageType());

        return storageService.toStorageConfiguration(storageForm);
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
