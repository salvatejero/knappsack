package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.models.ApplicationModel;

import java.util.List;

public interface SearchService {
    List<ApplicationModel> searchApplications(String criteria, User user, ApplicationType deviceType);

    <D> List<D> searchApplications(String criteria, User user, ApplicationType deviceType, Class<D> classModel);

}
