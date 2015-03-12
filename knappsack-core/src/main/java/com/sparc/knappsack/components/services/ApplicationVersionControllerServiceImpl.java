package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.KeyVaultEntry;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.EntityState;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.ResignErrorType;
import com.sparc.knappsack.exceptions.ApplicationVersionResignException;
import com.sparc.knappsack.forms.ApplicationVersionForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("applicationVersionControllerService")
public class ApplicationVersionControllerServiceImpl implements ApplicationVersionControllerService {

    @Qualifier("applicationVersionService")
    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Qualifier("keyVaultEntryService")
    @Autowired(required = true)
    private KeyVaultEntryService keyVaultEntryService;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Override
    public ApplicationVersion saveApplicationVersion(ApplicationVersionForm applicationVersionForm, boolean sendNotifications) throws ApplicationVersionResignException{

        if (applicationVersionForm != null && applicationVersionForm.getParentId() != null) {

            Application parentApplication = applicationService.get(applicationVersionForm.getParentId());
            if (parentApplication != null) {
                ApplicationVersion currentApplicationVersion = applicationVersionService.get(applicationVersionForm.getId());

                // Check whether we are editing or not.
                boolean editing = false;
                if (currentApplicationVersion != null) {
                    editing = true;
                }

                // Check what the current AppState of the applicationVersion is if the version already exists and is being edited.
                AppState currentAppState = determineCurrentAppState(currentApplicationVersion);

                // Make a copy of the original requested AppState in case the application version is to be resigned.
                AppState requestedAppState = applicationVersionForm.getAppState();

                Domain parentDomain = parentApplication.getOwnedGroup();

                // Only continue if the parent domain exists.
                if (parentDomain != null) {

                    // Set the application version AppState to Resigning if requested.
                    if (currentAppState != AppState.RESIGNING && applicationVersionForm.getKeyVaultEntryId() != null && applicationVersionForm.getKeyVaultEntryId() > 0 && domainService.isApplicationResignerEnabled(parentDomain)) {
                        applicationVersionForm.setAppState(AppState.RESIGNING);
                    }

                    // Save the applicationVersion
                    ApplicationVersion savedApplicationVersion = applicationVersionService.saveApplicationVersion(applicationVersionForm);

                    if (savedApplicationVersion != null && savedApplicationVersion.getId() != null && savedApplicationVersion.getId() > 0) {

                        // Update the application version form so that the user can be notified if an error occurs during the resigning staging process.
                        applicationVersionForm.setId(savedApplicationVersion.getId());
                        applicationVersionForm.setEditing(true);

                        // Resign the application if it was requested to be resigned and is not already being resigned
                        if (AppState.RESIGNING.equals(savedApplicationVersion.getAppState()) && !AppState.RESIGNING.equals(requestedAppState)) {
                            boolean resignSuccess = false;
                            KeyVaultEntry keyVaultEntry = keyVaultEntryService.get(applicationVersionForm.getKeyVaultEntryId());

                            resignSuccess = applicationVersionService.resign(savedApplicationVersion, requestedAppState, keyVaultEntry);

                            // Check if application version was successfully staged to be resigned and if not the the application version in an error state.
                            if (!resignSuccess) {

                                // TODO: refactor to send notifications whenever proper audit trail is in place so that admins being notified know why the application version is in the error state.
                                updateApplicationVersionState(savedApplicationVersion, AppState.ERROR, false);
                                throw new ApplicationVersionResignException(ResignErrorType.GENERIC);
                            }
                        }

                        // Send notifications only if the application version is newly persisted or it was being edited and the AppState changed
                        if (sendNotifications && ((currentAppState != null && !currentAppState.equals(savedApplicationVersion.getAppState()))
                                || (EntityState.NEWLY_PERSISTED.equals(savedApplicationVersion.getState()) || !editing))) {
                            sendNotifications(savedApplicationVersion, EventType.APPLICATION_VERSION_STATE_CHANGED);
                        }

                        return savedApplicationVersion;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean updateApplicationVersionState(Long applicationVersionId, AppState appState, boolean sendNotifications) {
        ApplicationVersion applicationVersion = applicationVersionService.get(applicationVersionId);

        return updateApplicationVersionState(applicationVersion, appState, sendNotifications);
    }

    private boolean updateApplicationVersionState(ApplicationVersion applicationVersion, AppState appState, boolean sendNotifications) {
        boolean success = false;

        if (applicationVersion != null && appState != null && !appState.equals(applicationVersion.getAppState())) {
            applicationVersionService.updateAppState(applicationVersion, appState);

            ApplicationVersion updatedApplicationVersion = applicationVersionService.get(applicationVersion.getId());
            if (updatedApplicationVersion != null && appState.equals(updatedApplicationVersion.getAppState())) {
                success = true;

                if (sendNotifications && !AppState.DISABLED.equals(updatedApplicationVersion.getAppState())) {
                    sendNotifications(updatedApplicationVersion, EventType.APPLICATION_VERSION_STATE_CHANGED);
                }
            }
        }

        return success;
    }

    private boolean sendNotifications(ApplicationVersion applicationVersion, EventType eventType) {
        boolean success = false;

        EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(eventType);
        if (deliveryMechanism != null) {
            success = deliveryMechanism.sendNotifications(applicationVersion);
        }

        return success;
    }

    private AppState determineCurrentAppState(ApplicationVersion applicationVersion) {
        AppState currentAppState = null;
        if (applicationVersion != null) {
            currentAppState = applicationVersion.getAppState();
        }

        return currentAppState;
    }

}
