package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.exceptions.ApplicationVersionResignException;
import com.sparc.knappsack.forms.ApplicationVersionForm;

public interface ApplicationVersionControllerService {

    public ApplicationVersion saveApplicationVersion(ApplicationVersionForm applicationVersion, boolean sendNotifications) throws ApplicationVersionResignException;

    public boolean updateApplicationVersionState(Long applicationVersionId, AppState appState, boolean sendNotifications);

}
