package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.forms.ApplicationVersionForm;
import com.sparc.knappsack.models.ApplicationVersionModel;

import java.util.List;

public interface ApplicationVersionService extends EntityService<ApplicationVersion> {

    /**
     * @param applicationVersionForm UploadApplicationVersion
     * @return ApplicationVersion - populated with data from the UploadApplicationVersion and added to the specified Application
     */
    ApplicationVersion saveApplicationVersion(ApplicationVersionForm applicationVersionForm);

    /**
     * @param appVersionId Long - The ID of the ApplicationVersion to update
     * @param appState AppState - the new AppState (changes the visibility of the ApplicationVersion)
     */
    void updateAppState(Long appVersionId, AppState appState);

    /**
     * @param applicationVersion ApplicationVersion - The ApplicationVersion to update.
     * @param appState AppState - the new AppState (changes the visibility of the ApplicationVersion)
     */
    void updateAppState(ApplicationVersion applicationVersion, AppState appState);

    List<ApplicationVersion> getAll();

    /**
     * @param organizationId Id of the organization
     * @return List<ApplicationVersion> - get all ApplicationVersions for the given Organization
     */
    List<ApplicationVersion> getAll(Long organizationId);

    List<ApplicationVersion> getAllByApplication(Long applicationId, AppState... appState);

    /**
     * @param organizationId Id of the organization
     * @param appStates AppState...
     * @return List<ApplicationVersion> - get all ApplicationVersions for a given Organization and specific AppStates
     */
    List<ApplicationVersion> getAll(Long organizationId, AppState... appStates);

    List<ApplicationVersion> getAll(List<Organization> organizations, AppState... appStates);

    /**
     * @param group Group
     * @param appStates AppState...
     * @return List<ApplicationVersion> - get all ApplicationVersions for a given Group and specific AppStates
     */
    List<ApplicationVersion> getAll(Group group, AppState... appStates);

    /**
     * @param application Application
     * @param appStates AppState...
     * @return List<ApplicationVersion> - get all versions of an Application with specified AppStates
     */
    List<ApplicationVersion> getAll(Application application, AppState... appStates);

    /**
     * @param applicationVersionId Long
     * @param includeInstallFile
     * @return ApplicationVersionModel
     */
    ApplicationVersionModel createApplicationVersionModel(Long applicationVersionId, boolean includeInstallFile);

    /**
     * @param applicationVersion
     * @param includeInstallFile
     * @return ApplicationVersionModel
     */
    ApplicationVersionModel createApplicationVersionModel(ApplicationVersion applicationVersion, boolean includeInstallFile);

    <D> D getApplicationVersionModel(Long applicationVersionId, Class<D> modelClass);

    <D> List<D> getApplicationVersionModels(Long applicationId, Class<D> modelClass, AppState... appStates);

    /**
     * @param applicationVersion ApplicationVersion to be resigned
     * @param requestedAppState AppState which was originally requested for resigning
     * @param keyVaultEntry KeyVaultEntry to use for resigning
     * @return Whether or not the application was staged to be resigned.
     */
    boolean resign(ApplicationVersion applicationVersion, final AppState requestedAppState, KeyVaultEntry keyVaultEntry);

    void deleteAllForApplication(Application application);

    boolean doesVersionExistForApplication(Long applicationId, String versionName);
}
