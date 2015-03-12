package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.SortOrder;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.*;

import java.util.List;

public interface UserService extends EntityService<User> {

    void save(User user);

    List<Group> getGroups(User user, SortOrder sortOrder);

    List<Group> getGroupsForActiveOrganization(User user, SortOrder sortOrder);

    List<GroupModel> getGroupModels(User user, SortOrder sortOrder);

    List<GroupModel> getGroupModelsForActiveOrganization(User user, SortOrder sortOrder);

    <D> List<D> getGroupModels(User user, Class<D> modelClass, SortOrder sortOrder);

    <D> List<D> getGroupModelsForActiveOrganization(User user, Class<D> modelClass, SortOrder sortOrder);

    List<User> getUsersByActiveOrganization(Organization organization);

    List<Organization> getOrganizations(User user, SortOrder sortOrder);

    <D> List<D> getOrganizationModels(User user, Class<D> modelClass, SortOrder sortOrder);

    List<OrganizationModel> getOrganizationModels(User user, SortOrder sortOrder);

    List<Organization> getAdministeredOrganizations(User user, SortOrder sortOrder);

    List<Group> getAdministeredGroups(User user, SortOrder sortOrder);

    <D> List<D> getAdministeredGroupModels(User user, Class<D> modelClass, SortOrder sortOrder);

//    List<ApplicationVersion> getApplicationVersions(User user);

    List<ApplicationVersion> getApplicationVersions(User user, Long applicationId, SortOrder sortOrder);

    List<Application> getApplicationsForUser(User user, ApplicationType deviceType);

    List<Application> getApplicationsForUser(User user);

    List<ApplicationModel> getApplicationModelsForUser(User user, ApplicationType deviceType);

    <D> List<D> getApplicationModelsForUser(User user, ApplicationType applicationType, Class<D> modelClass);

    List<ApplicationModel> getApplicationsForUserFiltered(User user, ApplicationType userDeviceType, Long groupId, Long categoryId, ApplicationType applicationType);

    List<Application> getApplicationsForUser(User user, ApplicationType deviceType, Long categoryId);

    List<ApplicationModel> getApplicationModelsForUser(User user, ApplicationType deviceType, Long categoryId);

    <D> List<D> getApplicationModelsForUser(User user, ApplicationType applicationType, Long categoryId, Class<D> modelClass);

    UserDomain addUserToGroup(User user, Group group, UserRole userRole);

    UserDomain addUserToGroup(User user, Long groupId, UserRole userRole);

    UserDomain addUserToOrganization(User user, Organization organization, UserRole userRole);

    UserDomain addUserToDomain(User user, Domain domain, UserRole userRole);

    UserDomain addUserToOrganization(User user, Long organizationId, UserRole userRole);

//    boolean isUserInDomain(User user, Long domainId, UserRole userRole);
//
//    boolean isUserInDomain(User user, Long domainId);
//
//    boolean isUserInGroup(User user, Group group, UserRole userRole);
//
//    boolean isUserInGroup(User user, Group group);
//
//    boolean isUserInOrganization(User user, Organization organization, UserRole userRole);

    boolean activate(Long userId, String code);

    boolean changePassword(User user, String password, boolean isTempPassword);

    User getByEmail(String email);

    List<User> get(List<Long> ids);

    List<Category> getCategoriesForUser(User user, ApplicationType deviceType, SortOrder sortOrder);

    List<CategoryModel> getCategoryModelsForUser(User user, ApplicationType applicationType, boolean includeIcons, SortOrder sortOrder);

    <D> List<D> getCategoryModelsForUser(User user, ApplicationType applicationType, Class<D> modelClass, SortOrder sortOrder);

    boolean updateSecurityContext(User user);

    User getUserFromSecurityContext();

    /**
     * @return long - a count of all the users in the system
     */
    long countAll();

    boolean canUserEditApplication(Long userId, Long applicationId);

    boolean canUserEditApplication(User user, Application application);

    UserModel createUsermodel(User user);

    void setActiveOrganization(User user, Long organizationId);

    void setDefaultActiveOrganization(User user);

    /**
     * @param user User
     * @return long - total amount of organizations this user is currently and administrator for
     */
    long countAdministeredOrganizations(User user);

    List<Contacts> getContacts(User user);
}
