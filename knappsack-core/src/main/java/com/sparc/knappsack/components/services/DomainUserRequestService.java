package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.DomainUserRequest;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.enums.UserRole;

import java.util.List;

public interface DomainUserRequestService extends EntityService<DomainUserRequest> {

    DomainUserRequest createDomainUserRequest(User user, String accessCode);

    List<DomainUserRequest> getAll(Long domainId);

    List<DomainUserRequest> getAll(Domain domain);

    List<DomainUserRequest> getAll(Long domainId, Status status);

    List<DomainUserRequest> getAll(Domain domain, Status status);

    boolean acceptRequest(DomainUserRequest domainUserRequest, UserRole userRole);

    boolean declineRequest(DomainUserRequest domainUserRequest);

    boolean doesRequestExist(User user, Domain domain, Status status);
}
