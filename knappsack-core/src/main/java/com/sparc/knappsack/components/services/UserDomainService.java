package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.entities.UserDomain;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.UserDomainModel;

import java.util.List;

public interface UserDomainService extends EntityService<UserDomain> {

    UserDomain get(User user, Long domainId, UserRole userRole);

    UserDomain get(User user, Long domainId);

    List<UserDomain> getAll(Long domainId);

    List<UserDomain> getAll(Long domainId, UserRole... userRoles);

    List<UserDomain> getAll(Long domainId, DomainType domainType, UserRole... userRoles);

    List<UserDomain> getUserDomainsForEmailsAndDomains(List<String> emails, List<Long> domainId);

    void removeUserDomainFromDomain(Long domainId, Long userId);

    void updateUserDomainRole(Long userId, Long domainId, UserRole userRole);

    void removeAllFromDomain(Long domainId);

    UserDomainModel createUserDomainModel(UserDomain userDomain);

    /**
     * @param user User
     * @param domainType DomainType
     * @param userRole UserRole
     * @return long - total amount of UserDomains of a specific type and role tied to this user
     */
    long countDomains(User user, DomainType domainType, UserRole userRole);

    void delete(UserDomain userDomain);
}
