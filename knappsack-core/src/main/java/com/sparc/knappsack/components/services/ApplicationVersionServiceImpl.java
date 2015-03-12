package com.sparc.knappsack.components.services;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;
import com.sparc.knappsack.components.dao.ApplicationVersionDao;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.mapper.Mapper;
import com.sparc.knappsack.enums.AppFileType;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.forms.ApplicationVersionForm;
import com.sparc.knappsack.models.ApplicationVersionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Transactional
@org.springframework.stereotype.Service("applicationVersionService")
public class ApplicationVersionServiceImpl implements ApplicationVersionService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationVersionServiceImpl.class);

    @Qualifier("applicationVersionDao")
    @Autowired(required = true)
    private ApplicationVersionDao applicationVersionDao;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Qualifier("groupService")
    @Autowired(required = true)
    private GroupService groupService;

    @Qualifier("storageConfigurationService")
    @Autowired(required = true)
    protected StorageConfigurationService storageConfigurationService;

    @Qualifier("storageServiceFactory")
    @Autowired(required = true)
    private StorageServiceFactory storageServiceFactory;

    @Qualifier("appFileService")
    @Autowired(required = true)
    private AppFileService appFileService;

    @Qualifier("keyVaultServiceFactory")
    @Autowired(required = true)
    private KeyVaultServiceFactory keyVaultServiceFactory;

    @Qualifier("applicationVersionUserStatisticService")
    @Autowired(required = true)
    private ApplicationVersionUserStatisticService applicationVersionUserStatisticService;

    @Qualifier("mapper")
    @Autowired(required = true)
    private Mapper mapper;

    @Override
    public void add(ApplicationVersion applicationVersion) {
        applicationVersionDao.add(applicationVersion);
    }

    @Transactional(readOnly = true)
    @Override
    public ApplicationVersion get(Long id) {
        ApplicationVersion applicationVersion = null;
        if (id != null && id > 0) {
            applicationVersion = applicationVersionDao.get(id);
        }
        return applicationVersion;
    }

    @Override
    public void delete(Long id) {
        delete(get(id));
    }

    @Override
    public void update(ApplicationVersion applicationVersion) {
        applicationVersionDao.update(applicationVersion);
    }

    private void save(ApplicationVersion applicationVersion) {
        if (applicationVersion != null) {
            if (applicationVersion.getId() != null && applicationVersion.getId() > 0) {
                update(applicationVersion);
            } else {
                add(applicationVersion);
            }
        }
    }

    private void delete(ApplicationVersion applicationVersion) {
        Application application = applicationVersion.getApplication();
        List<Group> groups = groupService.getGuestGroups(applicationVersion);
        for (Group group : groups) {
            group.getGuestApplicationVersions().remove(applicationVersion);
        }
        if (application != null && application.getApplicationVersions() != null) {

            applicationVersionUserStatisticService.deleteAllForApplicationVersion(applicationVersion);

            deleteFiles(applicationVersion);

            application.getApplicationVersions().remove(applicationVersion);
            applicationVersionDao.delete(applicationVersion);
        }
    }

    @Override
    public void updateAppState(Long appVersionId, AppState appState) {
        updateAppState(applicationVersionDao.get(appVersionId), appState);
    }

    @Override
    public void updateAppState(ApplicationVersion applicationVersion, AppState appState) {
        if (applicationVersion != null && appState != null) {
            applicationVersion.setAppState(appState);
            applicationVersionDao.update(applicationVersion);
        }
    }

    @Override
    public ApplicationVersion saveApplicationVersion(ApplicationVersionForm versionForm) {
        ApplicationVersion versionToSave = null;
        if (versionForm != null) {
            Application application = applicationService.get(versionForm.getParentId());
            if (application != null) {

                versionToSave = createApplicationVersion(versionForm, application);

                StorageService storageService = getStorageService(application.getStorageConfiguration());
                Group group = application.getOwnedGroup();
                Long orgStorageConfigId = group.getOrganization().getOrgStorageConfig().getId();

                // TODO: Refactor to throw errors if ios info not set
                setIOSPlistInfoOnApplication(versionForm, versionToSave);

                if (versionForm.getAppFile() != null) {
                    AppFile installationAppFile = storeInstallationFile(versionForm, orgStorageConfigId, application.getStorageConfiguration().getId(), storageService, versionToSave.getUuid());
                    setStorableOnAppFile(installationAppFile, versionToSave);
                    versionToSave.setInstallationFile(installationAppFile);
                }

                if (versionForm.getProvisioningProfile() != null) {
                    AppFile provisioningAppFile = storeProvisioningFile(versionForm, orgStorageConfigId, application.getStorageConfiguration().getId(), storageService, versionToSave.getUuid());
                    setStorableOnAppFile(provisioningAppFile, versionToSave);
                    versionToSave.setProvisioningProfile(provisioningAppFile);
                }

                if (versionToSave.getApplication() == null) {
                    versionToSave.setApplication(application);
                }

                List<Group> guestGroups = versionToSave.getGuestGroups();

                //Remove old guest groups
                List<Group> guestGroupsToRemove = new ArrayList<Group>();
                for (Group guestGroup : guestGroups) {
                    if (!versionForm.getGuestGroupIds().contains(guestGroup.getId())) {
                        guestGroupsToRemove.add(guestGroup);
                    }
                }
                for (Group guestGroupToRemove : guestGroupsToRemove) {
                    versionToSave.getGuestGroups().remove(guestGroupToRemove);
                }

                List<Long> guestGroupIds = versionForm.getGuestGroupIds();
                for (Long guestGroupId : guestGroupIds) {
                    Group guestGroup = groupService.get(guestGroupId);

                    if (!versionToSave.getGuestGroups().contains(guestGroup)) {
                        versionToSave.getGuestGroups().add(guestGroup);
                    }

                }

                if (!application.getApplicationVersions().contains(versionToSave)) {
                    application.getApplicationVersions().add(versionToSave);
                }

                return versionToSave;
            }
        }

        return versionToSave;
    }

    public List<ApplicationVersion> getAll() {
        return applicationVersionDao.getAll();
    }

    @Override
    public List<ApplicationVersion> getAll(Long organizationId) {
        return getAll(organizationId, AppState.values());
    }

    public List<ApplicationVersion> getAll(List<Organization> organizations, AppState... appStates) {
        if(organizations.isEmpty()) {
            return new ArrayList<ApplicationVersion>();
        }
        return applicationVersionDao.getAllByOrganizations(organizations, appStates);
    }

    @Override
    public List<ApplicationVersion> getAllByApplication(Long applicationId, AppState... appState) {
        return applicationVersionDao.getAllByApplication(applicationId, appState);
    }

    @Override
    public List<ApplicationVersion> getAll(Long organizationId, AppState... appStates) {
        return applicationVersionDao.getAllByOrganization(organizationId, appStates);
    }

    @Override
    public List<ApplicationVersion> getAll(Group group, AppState... appStates) {
        List<ApplicationVersion> versions = new ArrayList<ApplicationVersion>();
        if (group != null && appStates != null && appStates.length > 0) {
            List<Application> applications = group.getOwnedApplications();
            for (Application application : applications) {
                versions.addAll(getAll(application, appStates));
            }
        }
        return versions;
    }

    @Override
    public List<ApplicationVersion> getAll(Application application, AppState... appStates) {
        List<ApplicationVersion> versions = new ArrayList<ApplicationVersion>();
        if (application != null && appStates != null && appStates.length > 0) {
            for (ApplicationVersion version : application.getApplicationVersions()) {
                if (Arrays.asList(appStates).contains(version.getAppState())) {
                    versions.add(version);
                }
            }
        }
        return versions;
    }

    @Override
    public ApplicationVersionModel createApplicationVersionModel(Long applicationVersionId, boolean includeInstallFile) {
        return createApplicationVersionModel(get(applicationVersionId), includeInstallFile);
    }

    @Override
    public <D> D getApplicationVersionModel(Long applicationVersionId, Class<D> modelClass) {
        return mapper.map(get(applicationVersionId), modelClass);
    }

    @Override
    public <D> List<D> getApplicationVersionModels(Long applicationId, Class<D> modelClass, AppState... appStates) {
        List<ApplicationVersion> versions = getAllByApplication(applicationId, appStates);
        List<D> models = new ArrayList<D>();
        for (ApplicationVersion version : versions) {
            models.add(mapper.map(version, modelClass));
        }
        return models;
    }

    @Override
    public ApplicationVersionModel createApplicationVersionModel(ApplicationVersion applicationVersion, boolean includeInstallFile) {
        ApplicationVersionModel model = null;

        if (applicationVersion != null) {
            model = new ApplicationVersionModel();
            model.setId(applicationVersion.getId());
            model.setVersionName(applicationVersion.getVersionName());
            model.setRecentChanges(applicationVersion.getRecentChanges());
            model.setCfBundleIdentifier(applicationVersion.getCfBundleIdentifier());
            model.setCfBundleName(applicationVersion.getCfBundleName());
            model.setCfBundleVersion(applicationVersion.getCfBundleVersion());
            model.setAppState(applicationVersion.getAppState());
            if (includeInstallFile) {
                AppFile installationFile = applicationVersion.getInstallationFile();
                if (installationFile != null) {
                    model.setInstallationFile(appFileService.createAppFileModel(installationFile.getId()));
                }
            }
        }

        return model;
    }

    @Override
    public boolean resign(ApplicationVersion applicationVersion, final AppState requestedAppState, KeyVaultEntry keyVaultEntry) {
        boolean success = false;
        if (applicationVersion != null && requestedAppState != null && keyVaultEntry != null) {
            Application parentApplication = applicationVersion.getApplication();

            if (parentApplication != null && checkIfValidKeyVaultEntry(parentApplication, keyVaultEntry)) {

                // Check if applicationVersion appState is already set to resigning or not
                if (!AppState.RESIGNING.equals(applicationVersion.getAppState())) {
                    applicationVersion.setAppState(AppState.RESIGNING);
                    update(applicationVersion);
                }

                KeyVaultService keyVaultService = keyVaultServiceFactory.getKeyVaultService(keyVaultEntry.getApplicationType());
                if (keyVaultService != null) {
                    success = keyVaultService.resign(keyVaultEntry, applicationVersion, requestedAppState);
                }

            }
        }

        return success;
    }

    private boolean checkIfValidKeyVaultEntry(Application application, KeyVaultEntry keyVaultEntry) {
        boolean isValid = false;
        if (application != null && keyVaultEntry != null) {
            isValid = ApplicationType.getAllInGroup(keyVaultEntry.getApplicationType()).contains(application.getApplicationType());
        }
        return isValid;
    }

    @Override
    public void deleteAllForApplication(Application application) {
        if (application != null) {
            for (ApplicationVersion applicationVersion : application.getApplicationVersions()) {

                applicationVersion.setGuestGroups(null);

                applicationVersionUserStatisticService.deleteAllForApplicationVersion(applicationVersion);

                deleteFiles(applicationVersion);
                applicationVersion.setInstallationFile(null);
                applicationVersion.setProvisioningProfile(null);
            }

            for (ApplicationVersion applicationVersion : application.getApplicationVersions()) {
                applicationVersionDao.delete(applicationVersion);
            }
        }
    }

    private void deleteFiles(ApplicationVersion applicationVersion) {
        if (applicationVersion != null) {
            appFileService.delete(applicationVersion.getInstallationFile());
            appFileService.delete(applicationVersion.getProvisioningProfile());
        }
    }

    @Override
    public boolean doesVersionExistForApplication(Long applicationId, String versionName) {
        if (applicationId == null || applicationId <= 0 || !StringUtils.hasText(versionName)) {
            return false;
        }

        return applicationVersionDao.doesVersionExistForApplication(applicationId, StringUtils.trimTrailingWhitespace(versionName));
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

    private ApplicationVersion createApplicationVersion(ApplicationVersionForm applicationVersionForm, Application application) {
        ApplicationVersion applicationVersion;
        if (applicationVersionForm.getId() != null && applicationVersionForm.getId() > 0) {
            applicationVersion = get(applicationVersionForm.getId());
        } else {
            applicationVersion = new ApplicationVersion();
            applicationVersion.setApplication(application);
            applicationVersion.setStorageConfiguration(application.getStorageConfiguration());
            if (applicationVersionForm.getVersionName() != null && !"".equals(applicationVersionForm.getVersionName().trim())) {
                applicationVersion.setVersionName(applicationVersionForm.getVersionName());
            }
        }

        applicationVersion.setRecentChanges(applicationVersionForm.getRecentChanges());
        applicationVersion.setAppState(applicationVersionForm.getAppState());

        save(applicationVersion);

        return applicationVersion;
    }

    private AppFile storeInstallationFile(ApplicationVersionForm applicationVersionForm, Long orgStorageConfigId, Long storageConfigurationId, StorageService storageService, String uuid) {
        return storageService.save(applicationVersionForm.getAppFile(), AppFileType.INSTALL.getPathName() + storageService.getPathSeparator() + applicationVersionForm.getVersionName(), orgStorageConfigId, storageConfigurationId, uuid);
    }

    private AppFile storeProvisioningFile(ApplicationVersionForm applicationVersionForm, Long orgStorageConfigId, Long storageConfigurationId, StorageService storageService, String uuid) {
        return storageService.save(applicationVersionForm.getProvisioningProfile(), AppFileType.INSTALL.getPathName() + storageService.getPathSeparator() + applicationVersionForm.getVersionName(), orgStorageConfigId, storageConfigurationId, uuid);
    }

    private StorageService getStorageService(StorageConfiguration storageConfiguration) {
        return storageServiceFactory.getStorageService(storageConfiguration.getStorageType());
    }

    private void setStorableOnAppFile(AppFile appFile, Storable storable) {
        if (appFile != null) {
            appFile.setStorable(storable);
        }
    }

    private StorageConfiguration getStorageConfiguration(Long storageConfigurationId) {
        return storageConfigurationService.get(storageConfigurationId);
    }

    private void setIOSPlistInfoOnApplication(ApplicationVersionForm applicationVersionForm, ApplicationVersion applicationVersion) {
        if (applicationVersionForm.getAppFile() != null && applicationVersionForm.getAppFile().getOriginalFilename().toLowerCase().endsWith(".ipa")) {
            //TODO: Refactor cracking of iOS IPA if possible
            ZipInputStream inputStream = null;
            try {
                inputStream = new ZipInputStream(applicationVersionForm.getAppFile().getInputStream());
                ZipEntry zipEntry = inputStream.getNextEntry();
                while (zipEntry != null) {
                    String path = zipEntry.getName().replace(File.pathSeparatorChar, '/');
                    String[] splitString = path.split("/");
                    if (splitString != null && splitString.length == 3 && splitString[2].equalsIgnoreCase("info.plist")) {
                        break;
                    }
                    inputStream.closeEntry();
                    zipEntry = inputStream.getNextEntry();
                }

                if (zipEntry != null) {
                    NSDictionary object = (NSDictionary) PropertyListParser.parse(inputStream);
                    if (object != null) {
                        NSString cfBundleIdentifier = (NSString) object.objectForKey("CFBundleIdentifier");
                        NSString cfBundleVersion = (NSString) object.objectForKey("CFBundleVersion");
                        NSString cfBundleName = (NSString) object.objectForKey("CFBundleName");

                        applicationVersion.setCfBundleIdentifier(cfBundleIdentifier.toString());
                        applicationVersion.setCfBundleVersion(cfBundleVersion.toString());
                        applicationVersion.setCfBundleName(cfBundleName.toString());
                    }
                    inputStream.closeEntry();
                }
            } catch (IOException e) {
                log.error("IOException caught while extracting IOS data", e);
            } catch (Exception e) {
                log.error("Exception caught while extracting IOS data", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("IOException caught closing inputStream while extracting IOS data", e);
                    }
                }
            }
        }
    }
}
