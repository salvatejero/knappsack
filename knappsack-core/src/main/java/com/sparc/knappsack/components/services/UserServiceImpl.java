package com.sparc.knappsack.components.services;

import com.sparc.knappsack.comparators.ApplicationVersionComparator;
import com.sparc.knappsack.comparators.CategoryNameComparator;
import com.sparc.knappsack.comparators.GroupNameComparator;
import com.sparc.knappsack.comparators.OrganizationNameComparator;
import com.sparc.knappsack.components.dao.*;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.mapper.Mapper;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.SortOrder;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Transactional( propagation = Propagation.REQUIRED )
@Service("userService")
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Qualifier("userDomainService")
    @Autowired
    private UserDomainService userDomainService;

    @Qualifier("organizationDao")
    @Autowired
    private OrganizationDao organizationDao;

    @Qualifier("roleService")
    @Autowired
    private RoleService roleService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Qualifier("categoryService")
    @Autowired(required = true)
    private CategoryService categoryService;

    @Qualifier("userDetailsDao")
    @Autowired(required = true)
    private UserDetailsDao userDetailsDao;

    @Autowired(required = true)
    private GroupService groupService;

    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("passwordEncoder")
    @Autowired(required = true)
    private PasswordEncoder passwordEncoder;

    @Qualifier("applicationDao")
    @Autowired(required = true)
    private ApplicationDao applicationDao;

    @Qualifier("applicationVersionDao")
    @Autowired(required = true)
    private ApplicationVersionDao applicationVersionDao;

    @Qualifier("groupDao")
    @Autowired(required = true)
    private GroupDao groupDao;

    @Qualifier("categoryDao")
    @Autowired(required = true)
    private CategoryDao categoryDao;

    @Qualifier("mapper")
    @Autowired(required = true)
    private Mapper mapper;

    @Override
    public User get(Long id) {
        User user = null;
        if (id != null && id > 0) {
            user = userDetailsDao.get(id);
        }
        return user;
    }

    @Override
    public void add(User user) {
        userDetailsDao.add(user);
    }

    @Override
    public void delete(Long id) {
        userDetailsDao.delete(get(id));
    }

    @Override
    public void update(User user) {
        userDetailsDao.update(user);
    }

    @Override
    public void save(User user) {
        if (user != null) {
            if (user.getId() != null && user.getId() > 0) {
                userDetailsDao.update(user);
            } else {
                userDetailsDao.add(user);
            }
        }
    }

    @Override
    public List<Group> getGroups(User user, SortOrder sortOrder) {

        TreeSet<Group> groups = new TreeSet<Group>(new GroupNameComparator());

        if (user != null) {
            groups.addAll(groupDao.getGroupsForUser(user));
        }

        List<Group> sortedList = new ArrayList<Group>();
        if (SortOrder.ASCENDING.equals(sortOrder)) {
            sortedList.addAll(groups);
        } else if (SortOrder.DESCENDING.equals(sortOrder)) {
            sortedList.addAll(groups.descendingSet());
        } else {
            sortedList.addAll(groups);
        }

        return sortedList;
    }

    @Override
    public List<Group> getGroupsForActiveOrganization(User user, SortOrder sortOrder) {
        TreeSet<Group> groups = new TreeSet<Group>(new GroupNameComparator());

        if (user != null) {
            groups.addAll(groupDao.getGroupsForUserActiveOrganization(user));
        }

        List<Group> sortedList = new ArrayList<Group>();
        if (SortOrder.ASCENDING.equals(sortOrder)) {
            sortedList.addAll(groups);
        } else if (SortOrder.DESCENDING.equals(sortOrder)) {
            sortedList.addAll(groups.descendingSet());
        } else {
            sortedList.addAll(groups);
        }

        return sortedList;
    }

    @Override
    public List<GroupModel> getGroupModels(User user, SortOrder sortOrder) {
        List<GroupModel> groupModels = new ArrayList<GroupModel>();
        List<Group> groups = getGroups(user, sortOrder);
        for (Group group : groups) {
            groupModels.add(groupService.createGroupModel(group));
        }

        return groupModels;
    }

    @Override
    public List<GroupModel> getGroupModelsForActiveOrganization(User user, SortOrder sortOrder) {
        List<GroupModel> groupModels = new ArrayList<GroupModel>();
        List<Group> groups = getGroupsForActiveOrganization(user, sortOrder);
        for (Group group : groups) {
            groupModels.add(groupService.createGroupModel(group));
        }

        return groupModels;
    }

    @Override
    public <D> List<D> getGroupModels(User user, Class<D> modelClass, SortOrder sortOrder) {
        List<D> groupModels = new ArrayList<D>();
        List<Group> groups = getGroups(user, sortOrder);
        for (Group group : groups) {
            groupModels.add(mapper.map(group, modelClass));
        }
        return groupModels;
    }

    @Override
    public <D> List<D> getGroupModelsForActiveOrganization(User user, Class<D> modelClass, SortOrder sortOrder) {
        List<D> groupModels = new ArrayList<D>();
        List<Group> groups = getGroupsForActiveOrganization(user, sortOrder);
        for (Group group : groups) {
            groupModels.add(mapper.map(group, modelClass));
        }
        return groupModels;
    }

    public List<User> getUsersByActiveOrganization(Organization organization) {
        return userDetailsDao.getUsersByActiveOrganization(organization);
    }

    @Override
    public List<Organization> getOrganizations(User user, SortOrder sortOrder) {
        TreeSet<Organization> organizations = new TreeSet<Organization>(new OrganizationNameComparator());

        if(user.isSystemAdmin()) {
            organizations.addAll(organizationDao.getAll());
        } else {

            List<Domain> domains = domainService.getAll(user, DomainType.ORGANIZATION, DomainType.GROUP);
            for (Domain domain : domains) {
                if(DomainType.ORGANIZATION.equals(domain.getDomainType())) {
                    organizations.add((Organization) domain);
                } else if(DomainType.GROUP.equals(domain.getDomainType())) {
                    organizations.add(((Group)domain).getOrganization());
                }
            }
        }

        List<Organization> sortedList = new ArrayList<Organization>();
        if (SortOrder.ASCENDING.equals(sortOrder)) {
            sortedList.addAll(organizations);
        } else if (SortOrder.DESCENDING.equals(sortOrder)) {
            sortedList.addAll(organizations.descendingSet());
        } else {
            sortedList.addAll(organizations);
        }

        return sortedList;
    }

    @Override
    public <D> List<D> getOrganizationModels(User user, Class<D> modelClass, SortOrder sortOrder) {
        List<D> orgModels = new ArrayList<D>();
        List<Organization> organizations = getOrganizations(user, sortOrder);
        for (Organization organization : organizations) {
            orgModels.add(mapper.map(organization, modelClass));
        }

        return orgModels;
    }

    @Override
    public List<OrganizationModel> getOrganizationModels(User user, SortOrder sortOrder) {
        return organizationService.createOrganizationModels(getOrganizations(user, sortOrder), false, sortOrder);
    }

    @Override
    public List<Organization> getAdministeredOrganizations(User user, SortOrder sortOrder) {
        TreeSet<Organization> organizations = new TreeSet<Organization>(new OrganizationNameComparator());

        if (user != null) {
            organizations.addAll(organizationDao.getAdministeredOrganizationsForUser(user));
        }

        List<Organization> sortedList = new ArrayList<Organization>();
        if (SortOrder.ASCENDING.equals(sortOrder)) {
            sortedList.addAll(organizations);
        } else if (SortOrder.DESCENDING.equals(sortOrder)) {
            sortedList.addAll(organizations.descendingSet());
        } else {
            sortedList.addAll(organizations);
        }

        return sortedList;
    }

    @Override
    public List<Group> getAdministeredGroups(User user, SortOrder sortOrder) {
        TreeSet<Group> groups = new TreeSet<Group>(new GroupNameComparator());

        if (user != null) {

            if(user.isSystemAdmin() && user.getActiveOrganization() != null) {
                groups.addAll(user.getActiveOrganization().getGroups());
            } else {
                groups.addAll(groupDao.getAdministeredGroupsForUser(user));
            }
        }

        List<Group> sortedList = new ArrayList<Group>();
        if (SortOrder.ASCENDING.equals(sortOrder)) {
            sortedList.addAll(groups);
        } else if (SortOrder.DESCENDING.equals(sortOrder)) {
            sortedList.addAll(groups.descendingSet());
        } else {
            sortedList.addAll(groups);
        }

        return new ArrayList<Group>(groups);
    }

    @Override
    public <D> List<D> getAdministeredGroupModels(User user, Class<D> modelClass, SortOrder sortOrder) {
        List<D> groupModels = new ArrayList<D>();
        for (Group group : getAdministeredGroups(user, sortOrder)) {
            groupModels.add(mapper.map(group, modelClass));
        }

        return groupModels;
    }

    @Override
    public long countAdministeredOrganizations(User user) {
        return userDomainService.countDomains(user, DomainType.ORGANIZATION, UserRole.ROLE_ORG_ADMIN);
    }

    @Override
    public List<Contacts> getContacts(User user) {
        List<Contacts> contactsList = new ArrayList<Contacts>();
        Organization organization = user.getActiveOrganization();
        if(organization == null) {
            return contactsList;
        }
        Contacts contacts = new Contacts();
        contacts.setDomainName(organization.getName());
        Set<User> users = organizationService.getAllAdmins(organization, false);
        for (User admin : users) {
            Contact contact = new Contact();
            contact.setName(admin.getFullName());
            contact.setEmail(admin.getEmail());
            contacts.getContacts().add(contact);
        }
        contactsList.add(contacts);

        return contactsList;
    }

    @Override
    public User getByEmail(String email) {
        User user = null;
        if (email != null && !"".equals(email.trim())) {
            user = userDetailsDao.findByEmail(email.trim());
        }
        return user;
    }

    public List<User> get(List<Long> ids) {
        return userDetailsDao.get(ids);
    }

//    public List<ApplicationVersion> getApplicationVersions(User user) {
//
//        List<Application>
//
//        List<Group> groups = getGroups(user);
//        Set<ApplicationVersion> applicationVersions = new HashSet<ApplicationVersion>();
//        for (Group group : groups) {
//            applicationVersions.addAll(group.getGuestApplicationVersions());
//            List<Application> applications = group.getOwnedApplications();
//            for (Application application : applications) {
//                applicationVersions.addAll(application.getApplicationVersions());
//            }
//        }
//
//        Organization activeOrganization = user.getActiveOrganization();
//        if(activeOrganization != null) {
//            applicationVersions.addAll(applicationVersionService.getAll(activeOrganization.getId(), AppState.ORGANIZATION_PUBLISH, AppState.ORG_PUBLISH_REQUEST));
//        }
//
//        return new ArrayList<ApplicationVersion>(applicationVersions);
//    }

    @Override
    public List<ApplicationVersion> getApplicationVersions(User user, Long applicationId, SortOrder sortOrder) {
        TreeSet<ApplicationVersion> versionsSet = new TreeSet<ApplicationVersion>(new ApplicationVersionComparator());
        if (user != null && applicationId != null && applicationId > 0) {
            List<ApplicationVersion> returnedVersions = applicationVersionDao.getAllByApplicationForUser(applicationId, user);
            if (returnedVersions != null) {
                versionsSet.addAll(returnedVersions);
            }
        }

        List<ApplicationVersion> sortedList = new ArrayList<ApplicationVersion>();
        if (SortOrder.ASCENDING.equals(sortOrder)) {
            sortedList.addAll(versionsSet);
        } else if (SortOrder.DESCENDING.equals(sortOrder)) {
            sortedList.addAll(versionsSet.descendingSet());
        } else {
            sortedList.addAll(versionsSet);
        }

        return new ArrayList<ApplicationVersion>(sortedList);
    }

    @Override
    public List<Application> getApplicationsForUser(User user, ApplicationType deviceType) {
        Set<Application> applications = new HashSet<Application>();
        if (user != null && deviceType != null) {
            List<ApplicationType> allForUserDeviceType = ApplicationType.getAllForUserDeviceType(deviceType);
            applications.addAll(applicationDao.getAllForUser(user, allForUserDeviceType.toArray(new ApplicationType[allForUserDeviceType.size()])));
        }
        return new ArrayList<Application>(applications);
    }

    @Override
    public List<Application> getApplicationsForUser(User user) {
        Set<Application> applications = new HashSet<Application>();
        if (user != null) {
            applications.addAll(applicationDao.getAllForUser(user));
        }

        return new ArrayList<Application>(applications);
    }

    @Override
    public List<ApplicationModel> getApplicationModelsForUser(User user, ApplicationType deviceType) {
        List<Application> applications = getApplicationsForUser(user, deviceType);
        List<ApplicationModel> applicationModels = new ArrayList<ApplicationModel>();
        for (Application application : applications) {
            ApplicationModel applicationModel = applicationService.createApplicationModel(application, false);
            if (applicationModel != null) {
                applicationModel.setCanUserEdit(canUserEditApplication(user, application));
            }

            applicationModels.add(applicationModel);
        }

        return applicationModels;
    }

    @Override
    public <D> List<D> getCategoryModelsForUser(User user, ApplicationType applicationType, Class<D> modelClass, SortOrder sortOrder) {
        List<Category> categories = getCategoriesForUser(user, applicationType, sortOrder);
        List<D> categoryModels = new ArrayList<D>();
        for (Category category : categories) {
            categoryModels.add(mapper.map(category, modelClass));
        }

        return categoryModels;
    }

    @Override
    public List<ApplicationModel> getApplicationsForUserFiltered(User user, ApplicationType userDeviceType, Long groupId, Long categoryId, ApplicationType applicationType) {
        List<ApplicationModel> applicationModels = new ArrayList<ApplicationModel>();

        Group group = groupService.get(groupId);
        Category category = categoryService.get(categoryId);

        if (user == null || userDeviceType == null || (group == null && category == null && applicationType == null)) {
            return applicationModels;
        }

        // Get all available applications for the given user
        List<Application> applications = getApplicationsForUser(user, userDeviceType);

        Set<Application> filteredApplications = new HashSet<Application>();
        if (applications != null) {
            for (Application application : applications) {

                boolean valid = false;

                // Filter for group
                if (group != null) {
                    // If the specified group owns the application
                    if (group.getOwnedApplications() != null && group.getOwnedApplications().contains(application)) {
                        valid = true;
                    }

                    if (!valid) {
                        // If the group has guest access to any of the applications versions
                        List<ApplicationVersion> applicationVersions = application.getApplicationVersions();
                        if (applicationVersions != null) {
                            for (ApplicationVersion applicationVersion : applicationVersions) {
                                if (group.getGuestApplicationVersions() != null && group.getGuestApplicationVersions().contains(applicationVersion)) {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                // Filter for Category
                if (category != null && category.equals(application.getCategory())) {
                    valid = true;
                }

                // Filter for ApplicationType
                if (applicationType != null && applicationType.equals(application.getApplicationType())) {
                    valid = true;
                }

                if (valid) {
                    filteredApplications.add(application);
                }
            }
        }

        for (Application application : filteredApplications) {
            ApplicationModel applicationModel = applicationService.createApplicationModel(application, false);
            if (applicationModel != null) {
                applicationModel.setCanUserEdit(canUserEditApplication(user, application));
                applicationModels.add(applicationModel);
            }
        }

        return applicationModels;
    }

    @Override
    public List<Application> getApplicationsForUser(User user, ApplicationType deviceType, Long categoryId) {
        List<Application> applications;
        Category category = categoryService.get(categoryId);

        List<Application> appsToReturn = new ArrayList<Application>();

        if (category != null) {
            applications = getApplicationsForUser(user, deviceType);

            if (applications != null) {
                for (Application application : applications) {
                    if (application.getCategory().getId().equals(category.getId())) {
                        appsToReturn.add(application);
                    }
                }
            }
        }


        return appsToReturn;
    }

    @Override
    public List<ApplicationModel> getApplicationModelsForUser(User user, ApplicationType deviceType, Long categoryId) {
        List<Application> applications = getApplicationsForUser(user, deviceType, categoryId);
        List<ApplicationModel> applicationModels = new ArrayList<ApplicationModel>();
        for (Application application : applications) {
            ApplicationModel applicationModel = applicationService.createApplicationModel(application, false);
            if (applicationModel != null) {
                applicationModel.setCanUserEdit(canUserEditApplication(user, application));
            }

            applicationModels.add(applicationModel);
        }

        return applicationModels;
    }

    @Override
    public <D> List<D> getApplicationModelsForUser(User user, ApplicationType applicationType, Long categoryId, Class<D> modelClass) {
        List<Application> applications = getApplicationsForUser(user, applicationType, categoryId);
        List<D> applicationModels = new ArrayList<D>();
        for (Application application : applications) {
            applicationModels.add(mapper.map(application, modelClass));
        }

        return applicationModels;
    }

    @Override
    public UserDomain addUserToGroup(User user, Long groupId, UserRole userRole) {
        Group group = groupService.get(groupId);
        return addUserToGroup(user, group, userRole);
    }

    @Override
    public UserDomain addUserToGroup(User user, Group group, UserRole userRole) {
        if (user != null && group != null && userRole != null) {
            UserDomain userDomain = userDomainService.get(user, group.getId(), userRole);
            if(userDomain == null) {
                Role role = roleService.getRoleByAuthority(userRole.toString());
                userDomain = new UserDomain();
                userDomain.setDomain(group);
                userDomain.setRole(role);
                userDomain.setUser(userDetailsDao.get(user.getId()));

                userDomainService.add(userDomain);
                user.getUserDomains().add(userDomain);
            }

            return userDomain;
        }

        return null;
    }

    @Override
    public UserDomain addUserToOrganization(User user, Long organizationId, UserRole userRole) {
        Organization organization = organizationService.get(organizationId);
        return addUserToOrganization(user, organization, userRole);
    }

    @Override
    public UserDomain addUserToOrganization(User user, Organization organization, UserRole userRole) {
        if (user != null && organization != null && userRole != null) {
            UserDomain userDomain = userDomainService.get(user, organization.getId(), userRole);
            if(userDomain == null) {
                Role role = roleService.getRoleByAuthority(userRole.toString());
                userDomain = new UserDomain();
                userDomain.setDomain(organization);
                userDomain.setRole(role);
                userDomain.setUser(userDetailsDao.get(user.getId()));

                userDomainService.add(userDomain);
                user.getUserDomains().add(userDomain);
            }

            return userDomain;
        }

        return null;
    }

    @Override
    public UserDomain addUserToDomain(User user, Domain domain, UserRole userRole) {
        if(DomainType.GROUP.equals(domain.getDomainType()) && DomainType.GROUP.equals(userRole.getDomainType())) {
            return addUserToGroup(user, domain.getId(), userRole);
        } else if(DomainType.ORGANIZATION.equals(domain.getDomainType()) && DomainType.ORGANIZATION.equals(userRole.getDomainType())) {
            return addUserToOrganization(user, domain.getId(), userRole);
        }

        return null;
    }

    @Override
    public boolean activate(Long userId, String code) {
        boolean success = false;
        User user = get(userId);
        if (user != null && StringUtils.hasText(code) && StringUtils.hasText(user.getActivationCode())) {

            if (!user.isActivated() && user.getActivationCode().equals(code.trim())) {
                user.setActivated(true);
                setDefaultActiveOrganization(user);
                save(user);
                return true;
            }
        }
        return success;
    }

    @Override
    public boolean changePassword(User user, String password, boolean isTempPassword) {
        if (user != null && password != null && !"".equals(password)) {
            user.setPassword(passwordEncoder.encodePassword(password, user.getUsername()));
            user.setPasswordExpired(isTempPassword);

            save(user);

            return true;
        }
        return false;
    }

    @Override
    public List<Category> getCategoriesForUser(User user, ApplicationType deviceType, SortOrder sortOrder) {
        TreeSet<Category> categories = new TreeSet<Category>(new CategoryNameComparator());

        if (user != null && deviceType != null) {
            categories.addAll(categoryDao.getAllForUser(user, deviceType));
        }

        List<Category> sortedList = new ArrayList<Category>();
        if (SortOrder.ASCENDING.equals(sortOrder)) {
            sortedList.addAll(categories);
        } else if (SortOrder.DESCENDING.equals(sortOrder)) {
            sortedList.addAll(categories.descendingSet());
        } else {
            sortedList.addAll(categories);
        }

        return new ArrayList<Category>(categories);
    }

    @Override
    public List<CategoryModel> getCategoryModelsForUser(User user, ApplicationType applicationType, boolean includeIcons, SortOrder sortOrder) {
        List<CategoryModel> categoryModels = new ArrayList<CategoryModel>();
        List<Category> categories = getCategoriesForUser(user, applicationType, sortOrder);
        for (Category category : categories) {
            categoryModels.add(categoryService.createCategoryModel(category, includeIcons));
        }
        return categoryModels;
    }

    @Override
    public <D> List<D> getApplicationModelsForUser(User user, ApplicationType applicationType, Class<D> modelClass) {
        List<D> applicationModels = new ArrayList<D>();
        List<Application> applications = getApplicationsForUser(user, applicationType);
        for (Application application : applications) {
            applicationModels.add(mapper.map(application, modelClass));
        }

        return applicationModels;
    }

    @Override
    public boolean updateSecurityContext(User user) {

        try {
            User principal = getUserFromSecurityContext();
            if (principal != null && principal.equals(user)) {
                principal.setPassword(user.getPassword());
                principal.setPasswordExpired(user.isPasswordExpired());
                principal.setActivated(user.isActivated());

                return true;
            }
        } catch (Exception e) {
            log.error("Error updating security context:", e);
        }

        return false;
    }

    public User getUserFromSecurityContext() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null && context.getAuthentication().getPrincipal() != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return get(((User) principal).getId());
            }
        }

        return null;
    }

    @Override
    public long countAll() {
        return userDetailsDao.countAll();
    }

    @Override
    public boolean canUserEditApplication(Long userId, Long applicationId) {
        boolean canEdit = false;

        User user = get(userId);
        if (user != null) {
            canEdit = canUserEditApplication(user, applicationService.get(applicationId));
        }

        return canEdit;
    }

    @Override
    public boolean canUserEditApplication(User user, Application application) {
        boolean canEdit = false;

        if (user != null && application != null) {
            if(user.isActiveOrganizationAdmin() || user.isSystemAdmin()) {
                return true;
            }

            Group group = application.getOwnedGroup();//groupService.getOwnedGroup(application);
            if (group != null) {
                Organization organization = group.getOrganization();

                for (UserDomain userDomain : user.getUserDomains()) {
                    if (DomainType.ORGANIZATION.equals(userDomain.getDomain().getDomainType())) {
                        if (organization != null && userDomain.getDomain().equals(organization) && UserRole.ROLE_ORG_ADMIN.equals(userDomain.getRole().getUserRole())) {
                            canEdit = true;
                            break;
                        }
                    } else if (DomainType.GROUP.equals(userDomain.getDomain().getDomainType())) {
                        if (userDomain.getDomain().equals(group) && UserRole.ROLE_GROUP_ADMIN.equals(userDomain.getRole().getUserRole())) {
                            canEdit = true;
                            break;
                        }
                    }
                }
            }
        }

        return canEdit;
    }

    @Override
    public UserModel createUsermodel(User user) {
        UserModel model = null;
        if (user != null) {
            model = new UserModel();
            model.setId(user.getId());
            model.setEmail(user.getEmail());
            model.setFirstName(user.getFirstName());
            model.setLastName(user.getLastName());
            model.setUserName(user.getUsername());
        }

        return model;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

    @Override
    public void setActiveOrganization(User user, Long organizationId) {
        user.setActiveOrganization(organizationService.get(organizationId));
        update(user);
    }

    public void setDefaultActiveOrganization(User user) {
        if(user.getActiveOrganization() == null) {
            List<Organization> organizationList = getOrganizations(user, null);
            if(!organizationList.isEmpty()) {
                user.setActiveOrganization(organizationList.get(0));
            }
        }
    }
}
