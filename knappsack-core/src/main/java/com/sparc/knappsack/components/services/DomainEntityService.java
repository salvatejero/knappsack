package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.DomainModel;

import java.util.List;
import java.util.Set;

public interface DomainEntityService<Domain> {

    DomainModel createDomainModel(Domain domain);

    List<DomainModel> getAssignableDomainModelsForDomainRequest(Domain domain);

    List<User> getAllUsersForRole(Domain domain, UserRole userRole);

    boolean isApplicationResignerEnabled(Domain domain);

    boolean isDomainAdmin(Domain domain, User user);

    Set<User> getDomainRequestUsersForNotification(Domain domain);

    Set<User> getAllAdmins(Domain domain, boolean includeParentDomainAdminsIfEmpty);

}
