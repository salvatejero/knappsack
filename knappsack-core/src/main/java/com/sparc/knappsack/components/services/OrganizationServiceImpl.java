package com.sparc.knappsack.components.services;

import com.sparc.knappsack.comparators.OrganizationNameComparator;
import com.sparc.knappsack.components.dao.OrganizationDao;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.enums.*;
import com.sparc.knappsack.forms.OrganizationForm;
import com.sparc.knappsack.models.DomainModel;
import com.sparc.knappsack.models.OrganizationModel;
import com.sparc.knappsack.models.UserDomainModel;
import com.sparc.knappsack.util.ListSortUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Transactional( propagation = Propagation.REQUIRED )
@Service("organizationService")
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    @Qualifier("organizationDao")
    @Autowired(required = true)
    private OrganizationDao organizationDao;

    @Qualifier("storageConfigurationService")
    @Autowired
    private StorageConfigurationService storageConfigurationService;

    @Qualifier("userDomainService")
    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Qualifier("groupService")
    @Autowired(required = true)
    private GroupService groupService;

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Qualifier("storageServiceFactory")
    @Autowired
    private StorageServiceFactory storageServiceFactory;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @Qualifier("categoryService")
    @Autowired(required = true)
    private CategoryService categoryService;

    @Qualifier("keyVaultEntryService")
    @Autowired(required = true)
    private KeyVaultEntryService keyVaultEntryService;

    @Qualifier("appFileService")
    @Autowired(required = true)
    private AppFileService appFileService;

    @Override
    public void delete(Long id) {
        Organization organization = get(id);
        if (organization != null) {
            delete(organization);
        }
    }

    @Override
    public Organization get(Long id) {
        Organization organization = null;
        if (id != null && id > 0) {
            organization = organizationDao.get(id);
        }
        return organization;
    }

    @Override
    public void update(Organization organization) {
        organizationDao.update(organization);
    }

    private void save(Organization organization) {
        if (organization != null) {
            if (organization.getId() == null || organization.getId() <= 0) {
                organizationDao.add(organization);
            } else {
                organizationDao.update(organization);
            }
        }
    }

    private void delete(Organization organization) {
        if (organization != null) {
            userDomainService.removeAllFromDomain(organization.getId());
            invitationService.deleteAll(organization.getId());

            //Delete Applications
//            List<Application> applications = new ArrayList<Application>();
//            for (Group group : organization.getGroups()) {
//                applications.addAll(group.getOwnedApplications());
//                group.getGuestApplicationVersions().clear();
//            }
//            for (Application application : applications) {
//                applicationService.deleteApplicationFilesAndVersions(application);
//                application.getOwnedGroup().getOwnedApplications().remove(application);
//                application.setCategory(null);
//            }

            Set<Long> groupIds = new HashSet<Long>();
            for (Group group : organization.getGroups()) {
                groupIds.add(group.getId());
            }
            for (Long groupId : groupIds) {
                groupService.delete(groupId);
            }
            organization.getGroups().clear();

            //Delete Categories
            Set<Long> categoryIds = new HashSet<Long>();
            for (Category category : organization.getCategories()) {
                categoryIds.add(category.getId());
            }
            for (Long id : categoryIds) {
                categoryService.delete(id);
            }
            organization.getCategories().clear();

            CustomBranding customBranding = organization.getCustomBranding();
            if (customBranding != null) {
                appFileService.delete(customBranding.getLogo());
            }

            List<User> users = userService.getUsersByActiveOrganization(organization);
            for (User user : users) {
                List<Organization> userOrgs = userService.getOrganizations(user, null);
                if(userOrgs.size() > 0) {
                    user.setActiveOrganization(userOrgs.get(0));
                } else {
                    user.setActiveOrganization(null);
                }
            }

            keyVaultEntryService.deleteAllForDomain(organization);
            organization.setKeyVaultEntries(null);
            organization.setChildKeyVaultEntries(null);
            organizationDao.delete(organization);
        }
    }

    @Override
    public void add(Organization organization) {
        organizationDao.add(organization);
    }

    @Override
    public List<Organization> getOrganizations(List<Long> organizationIds) {
        return organizationDao.get(organizationIds);
    }

    @Override
    public Organization getByName(String name) {
        return organizationDao.get(name);
    }

    public List<Organization> getAll() {
        return organizationDao.getAll();
    }

    @Override
    public void mapOrgToOrgModel(Organization organization, OrganizationModel organizationModel) {
        if (organization != null && organizationModel != null) {
            organizationModel.setId(organization.getId());
            organizationModel.setName(organization.getName());
            if(organization.getOrgStorageConfig() != null) {
                organizationModel.setStorageConfigurationId(organization.getOrgStorageConfig().getStorageConfigurations().get(0).getId());
                organizationModel.setStoragePrefix(organization.getOrgStorageConfig().getPrefix());
            }
        }
    }

    @Override
    public Organization createOrganization(OrganizationForm organizationForm) {
        Organization organization = null;
        if (organizationForm != null) {
            organization = new Organization();

            organization.setDomainConfiguration(new DomainConfiguration());

            organization.setName(StringUtils.trimTrailingWhitespace(organizationForm.getName()));

            save(organization);

            StorageConfiguration storageConfiguration;
            if (organizationForm.getStorageConfigurationId() == null || organizationForm.getStorageConfigurationId() <= 0) {
                storageConfiguration = storageConfigurationService.getRegistrationDefault();
            } else {
                storageConfiguration = storageConfigurationService.get(organizationForm.getStorageConfigurationId());
            }
            if (storageConfiguration == null) {
                log.error("Attempted to create organization without a StorageConfiguration");
                return null;
            }

            OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
            if (StringUtils.hasText(organizationForm.getStoragePrefix())) {
                orgStorageConfig.setPrefix(organizationForm.getStoragePrefix().trim());
            } else {
                orgStorageConfig.setPrefix(organization.getUuid());
            }
            orgStorageConfig.getStorageConfigurations().add(storageConfiguration);
            orgStorageConfig.setOrganization(organization);
            organization.setOrgStorageConfig(orgStorageConfig);

            update(organization);

            CustomBranding customBranding = organization.getCustomBranding();
            if (customBranding == null) {
                customBranding = new CustomBranding();
                customBranding.setStorageConfiguration(storageConfiguration);
                customBranding.setEmailHeader(organizationForm.getEmailHeader().replaceAll("<br>", "<br/>"));
                customBranding.setEmailFooter(organizationForm.getEmailFooter().replaceAll("<br>", "<br/>"));
            }
            if (organizationForm.getLogo() != null) {
                AppFile logo = createLogo(organizationForm.getLogo(), organization);

                if (logo != null) {
                    logo.setStorable(customBranding);
                    customBranding.setLogo(logo);
                }
            }
            organization.setCustomBranding(customBranding);
            update(organization);

            User user = userService.getUserFromSecurityContext();
            if (user != null) {
                user.setActiveOrganization(organization);
            }
        }

        return organization;
    }

    @Override
    public void editOrganization(OrganizationForm organizationForm) {
        if (organizationForm != null) {
            Organization organization = get(organizationForm.getId());
            if (organization != null) {
                organization.setName(StringUtils.trimTrailingWhitespace(organizationForm.getName()));

                if (isCustomBrandingEnabled(organization)) {
                    CustomBranding customBranding = organization.getCustomBranding();
                    if (customBranding == null) {
                        customBranding = new CustomBranding();
                        customBranding.setStorageConfiguration(organization.getStorageConfigurations().get(0));
                    }

                    if(organizationForm.getLogo() != null) {
                        AppFile logo = createLogo(organizationForm.getLogo(), organization);
                        if (logo != null) {
                            logo.setStorable(customBranding);
                            customBranding.setLogo(logo);
                        }
                    }
                    customBranding.setEmailHeader(organizationForm.getEmailHeader().replaceAll("<br>", "<br/>"));
                    customBranding.setEmailFooter(organizationForm.getEmailFooter().replaceAll("<br>", "<br/>"));
                    customBranding.setSubdomain((organizationForm.getSubdomain()));
                    organization.setCustomBranding(customBranding);
                }

                save(organization);
            }
        }
    }

    @Override
    public void removeUserFromOrganization(Long organizationId, Long userId) {
        if (organizationId != null && organizationId > 0) {
            Organization organization = get(organizationId);
            if (organization != null) {

                //Remove user from all groups for the specified organization
                for (Group group : organization.getGroups()) {
                    groupService.removeUserFromGroup(group.getId(), userId);
                }

                //Remove user from the specified organization
                userDomainService.removeUserDomainFromDomain(organization.getId(), userId);
            }
        }
    }

    @Override
    public List<User> getAllUsersForRole(Organization organization, UserRole userRole) {
        List<User> users = new ArrayList<User>();
        if (organization != null && organization.getId() != null && organization.getId() > 0 && userRole != null) {
            List<UserDomain> userDomains = userDomainService.getAll(organization.getId(), userRole);
            users.addAll(getUsersFromUserDomains(userDomains));
        }
        return users;
    }


    private List<User> getUsersFromUserDomains(List<UserDomain> userDomains) {
        List<User> users = new ArrayList<User>();
        if (userDomains != null) {
            for (UserDomain userDomain : userDomains) {
                if (!users.contains(userDomain.getUser())) {
                    users.add(userDomain.getUser());
                }
            }
        }
        return users;
    }

//    @Override
//    public int getTotalApplications(Organization organization) {
//        int totalApplications = 0;
//        for (Group group : organization.getGroups()) {
//            totalApplications += groupService.getTotalApplications(group);
//        }
//        return totalApplications;
//    }
//
//    @Override
//    public int getTotalApplicationVersions(Organization organization) {
//        int totalApplicationVersions = 0;
//        for (Group group : organization.getGroups()) {
//            totalApplicationVersions += groupService.getTotalApplicationVersions(group);
//        }
//        return totalApplicationVersions;
//    }

    @Override
    public double getTotalMegabyteStorageAmount(Organization organization) {
        double totalMegabyteStorageAmount = 0;
        for (Group group : organization.getGroups()) {
            totalMegabyteStorageAmount += groupService.getTotalMegabyteStorageAmount(group);
        }

        return totalMegabyteStorageAmount;
    }

    @Override
    public List<UserDomainModel> getAllOrganizationMembers(Long organizationId, boolean includeGuests) {
        return getAllOrganizationMembers(get(organizationId), includeGuests);
    }

    @Override
    public List<UserDomainModel> getAllOrganizationMembers(Organization organization, boolean includeGuests) {
        Assert.notNull(organization, "Organization cannot be null");
        Map<Long, UserDomainModel> userDomainModels = new HashMap<Long, UserDomainModel>();

        for (UserDomain userDomain : userDomainService.getAll(organization.getId())) {
            UserDomainModel model = userDomainService.createUserDomainModel(userDomain);
            userDomainModels.put(userDomain.getUser().getId(), model);
        }

        if (includeGuests) {
            for (UserDomainModel guest : getAllOrganizationGuests(organization.getId())) {
                if (!userDomainModels.containsKey(guest.getUser().getId())) {
                    userDomainModels.put(guest.getUser().getId(), guest);
                }
            }
        }

        return new ArrayList<UserDomainModel>(userDomainModels.values());
    }

    @Override
    public List<UserDomainModel> getAllOrganizationGuests(Long organizationId) {
        return getAllOrganizationGuests(get(organizationId));
    }

    @Override
    public List<UserDomainModel> getAllOrganizationGuests(Organization organization) {
        Assert.notNull(organization, "Organization cannot be null");

        Map<Long, UserDomainModel> userDomainsModels = new HashMap<Long, UserDomainModel>();

        for (Group group : organization.getGroups()) {
            for (UserDomain userDomain : userDomainService.getAll(group.getId())) {
                if (!userDomainsModels.containsKey(userDomain.getUser().getId()) && userDomainService.get(userDomain.getUser(), organization.getId()) == null) {
                    UserDomainModel model = userDomainService.createUserDomainModel(userDomain);
                    userDomainsModels.put(userDomain.getUser().getId(), model);
                }
            }
        }

        return new ArrayList<UserDomainModel>(userDomainsModels.values());
    }

    @Override
    public boolean isApplicationLimit(Organization organization) {
        if (organization == null) {
            return true;
        }
        return organization.getDomainConfiguration().getApplicationLimit() <= countOrganizationApps(organization.getId());
    }

    @Override
    public boolean isUserLimit(Organization organization, boolean includeInvitations) {
        long totalUsers = countOrganizationUsers(organization.getId(), true);

        // Add invitation count for Organization and all child Groups to total count
        if (includeInvitations) {
            totalUsers += invitationService.countAllForOrganizationIncludingGroups(organization.getId());
        }
        return organization.getDomainConfiguration().getUserLimit() <= totalUsers;
    }

    @Override
    public boolean isBandwidthLimit(Organization organization, StorageType storageType, Date startDate, Date endDate) {
        boolean bandwidthLimitReached = false;
        StorageService storageService = storageServiceFactory.getStorageService(storageType);
        if (storageService instanceof RemoteStorageService) {
            DomainConfiguration domainConfiguration = organization.getDomainConfiguration();
            boolean checkingLimitValidations = !domainConfiguration.isDisableLimitValidations();
            boolean monitoringBandwidth = domainConfiguration.isMonitorBandwidth();
            long bandwidthLimitMB = domainConfiguration.getMegabyteBandwidthLimit();
            if (checkingLimitValidations && monitoringBandwidth) {
                double bandwidthUsed = ((RemoteStorageService) storageService).getMegabyteBandwidthUsed(organization.getOrgStorageConfig(), startDate, endDate);
                if (bandwidthUsed >= bandwidthLimitMB) {
                    bandwidthLimitReached = true;
                }
            }

            if(!domainConfiguration.isBandwidthLimitReached() && bandwidthLimitReached) {
                domainConfiguration.setBandwidthLimitReached(true);
                sendBandwidthNotification(organization);
            } else if(domainConfiguration.isBandwidthLimitReached() && !bandwidthLimitReached) {
                domainConfiguration.setBandwidthLimitReached(false);
            }
            update(organization);
        }

        return bandwidthLimitReached;
    }

    private boolean sendBandwidthNotification(Organization organization) {
        boolean success = false;
        EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.BANDWIDTH_LIMIT_REACHED);
        if (deliveryMechanism != null) {
            success = deliveryMechanism.sendNotifications(organization);
        }
        return success;
    }

    public List<OrganizationModel> createOrganizationModels(List<Organization> organizations, boolean includeExternalData, SortOrder sortOrder) {
        List<OrganizationModel> organizationModels = new ArrayList<OrganizationModel>();

        for (Organization organization : ListSortUtils.sortList(organizations, sortOrder, new OrganizationNameComparator())) {
            organizationModels.add(createOrganizationModel(organization, includeExternalData));
        }

        return organizationModels;
    }

    @Override
    public List<OrganizationModel> createOrganizationModelsWithoutStorageConfiguration(List<Organization> organizations, boolean includeExternalData, SortOrder sortOrder) {
        List<OrganizationModel> organizationModels = new ArrayList<OrganizationModel>();

        for (Organization organization : ListSortUtils.sortList(organizations, sortOrder, new OrganizationNameComparator())) {
            organizationModels.add(createOrganizationModelWithoutStorageConfiguration(organization, includeExternalData));
        }

        return organizationModels;
    }

    @Override
    public OrganizationModel createOrganizationModelWithoutStorageConfiguration(Organization organization, boolean includeExternalData) {
        OrganizationModel model = null;
        if (organization != null) {
            model = new OrganizationModel();
            model.setId(organization.getId());
            model.setName(organization.getName());
            if (organization.getCreateDate() != null) {
                model.setCreateDate(new Date(organization.getCreateDate().getTime()));
            }
        }
        return model;
    }

    @Override
    public OrganizationModel createOrganizationModel(Long organizationId, boolean includeExternalData) {
        return createOrganizationModel(get(organizationId), includeExternalData);
    }

    @Override
    public OrganizationModel createOrganizationModel(Organization organization, boolean includeExternalData) {
        OrganizationModel model = createOrganizationModelWithoutStorageConfiguration(organization, includeExternalData);

        OrgStorageConfig orgStorageConfig = organization.getOrgStorageConfig();
        if (orgStorageConfig != null && orgStorageConfig.getStorageConfigurations() != null) {
            model.setStorageConfigurationId(orgStorageConfig.getStorageConfigurations().get(0).getId());
            model.setStoragePrefix(orgStorageConfig.getPrefix());
        }
        return model;
    }

    @Override
    public DomainModel createDomainModel(Organization domain) {
        DomainModel model = null;
        if (domain != null) {
            model = createOrganizationModel(domain, false);
        }
        return model;
    }

    @Override
    public List<DomainModel> getAssignableDomainModelsForDomainRequest(Organization organization) {
        List<DomainModel> domainModels = new ArrayList<DomainModel>();
        domainModels.add(createOrganizationModel(organization, false));
        for(Group group : organization.getGroups()) {
            domainModels.add(groupService.createDomainModel(group));
        }

        return domainModels;
    }

    @Override
    public List<Application> getAllOrganizationApplications(Long organizationId) {
        List<Application> applications = new ArrayList<Application>();

        Organization organization = get(organizationId);
        if (organization != null) {
            for (Group group : organization.getGroups()) {
                applications.addAll(group.getOwnedApplications());
            }
        }
        return applications;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public long countAll() {
        return organizationDao.countAll();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public List<OrganizationModel> getAllOrganizationsForCreateDateRange(Date minDate, Date maxDate) {
        final List<OrganizationModel> models = new ArrayList<OrganizationModel>();
        List<Organization> organizations = new ArrayList<Organization>();
        if (minDate == null && maxDate == null) {
            organizations.addAll(organizationDao.getAll());
        } else {
            organizations.addAll(organizationDao.getAllForCreateDateRange(minDate, maxDate));
        }

        if (organizations != null) {

            ExecutorService ex = Executors.newCachedThreadPool();

            Collection<Callable<OrganizationModel>> tasks = new LinkedList<Callable<OrganizationModel>>();
            for (final Organization organization : organizations) {
                Callable<OrganizationModel> callable = new Callable<OrganizationModel>() {
                    @Override
                    public OrganizationModel call() throws Exception {
                        return createOrganizationModel(organization, true);
                    }
                };

                tasks.add(callable);
            }
            try {
                for (Future<OrganizationModel> future : ex.invokeAll(tasks)) {
                    models.add(future.get());
                }
            } catch (Exception e) {
                log.error("Error processing getAllOrganizationsForCreateDateRange:", e);
            }
        }
//        List<CustomerModel> customerModels = customerService.getAllCustomersForCreateDateRange(minDate, maxDate);
//        if (CollectionUtils.isEmpty(customerModels)) {
//            return models;
//        }
//
//        Map<Long, CustomerModel> customerModelMap = new HashMap<Long, CustomerModel>();
//        for (CustomerModel customerModel : customerModels) {
//            if (customerModel.getOrganizationId() != null && customerModel.getOrganizationId() > 0) {
//                customerModelMap.put(customerModel.getOrganizationId(), customerModel);
//            }
//        }
//
//        List<Organization> organizations = new ArrayList<Organization>();
//        if (minDate == null && maxDate == null) {
//            organizations.addAll(organizationDao.getAll());
//        } else {
//            organizations.addAll(organizationDao.getAllForCreateDateRange(minDate, maxDate));
//        }
//
//        for (Organization organization : organizations) {
//            CustomerModel customerModel = customerModelMap.get(organization.getId());
//            if (customerModel != null) {
//                OrganizationModel model = createOrganizationModel(organization, false);
//                if (model != null) {
//                    model.setCustomer(customerModel);
//                    models.add(model);
//                }
//            }
//        }
        return models;
    }

    @Override
    public List<UserDomainModel> getAllOrganizationAdmins(Long organizationId) {
        List<UserDomainModel> models = new ArrayList<UserDomainModel>();
        for(UserDomain userDomain : userDomainService.getAll(organizationId, DomainType.ORGANIZATION, UserRole.ROLE_ORG_ADMIN)) {
            models.add(userDomainService.createUserDomainModel(userDomain));
        }

        return models;
    }

    @Override
    public long countOrganizationUsers(Long organizationId, boolean includeGroups) {
        if (organizationId == null || organizationId <= 0) {
            return 0;
        }
        return organizationDao.countOrganizationUsers(organizationId, includeGroups);
    }

    @Override
    public long countOrganizationApps(Long organizationId) {
        if (organizationId == null || organizationId <= 0) {
            return 0;
        }
        return organizationDao.countOrganizationApps(organizationId);
    }

    @Override
    public long countOrganizationAppVersions(Long organizationId) {
        if (organizationId == null || organizationId <= 0) {
            return 0;
        }
        return organizationDao.countOrganizationAppVersions(organizationId);
    }

    @Override
    public long countOrganizationGroups(Long organizationId) {
        if (organizationId == null || organizationId <= 0) {
            return 0;
        }
        return organizationDao.countOrganizationGroups(organizationId);
    }

    @Override
    public boolean isDomainAdmin(Organization organization, User user) {
        for(UserDomain userDomain : user.getUserDomains()) {
            if(userDomain.getDomain().equals(organization) && UserRole.ROLE_ORG_ADMIN.name().equals(userDomain.getRole().getAuthority())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<User> getDomainRequestUsersForNotification(Organization organization) {
        Set<User> users = new HashSet<User>();
        for(UserDomain userDomain : organization.getUserDomains()) {
            if(isDomainAdmin(organization, userDomain.getUser())) {
                users.add(userDomain.getUser());
            }
        }

        return users;
    }

    @Override
    public boolean isApplicationResignerEnabled(Organization organization) {
        boolean isResignerEnabled = false;

        if (organization != null) {
            DomainConfiguration domainConfiguration = organization.getDomainConfiguration();
            if (domainConfiguration != null && domainConfiguration.isApplicationResignerEnabled()) {
                isResignerEnabled = true;
            }
        }

        return isResignerEnabled;
    }

    @Override
    public boolean isCustomBrandingEnabled(Organization organization) {
        boolean isCustomBrandingEnabled = false;

        if (organization != null) {
            DomainConfiguration domainConfiguration = organization.getDomainConfiguration();
            if (domainConfiguration != null && domainConfiguration.isCustomBrandingEnabled()) {
                isCustomBrandingEnabled = true;
            }
        }

        return isCustomBrandingEnabled;
    }

    @Override
    public Set<User> getAllAdmins(Organization organization, boolean includeParentDomainAdminsIfEmpty) {
        Set<User> users = new HashSet<User>();
        if (organization != null) {
            List<UserDomain> organizationAdmins = userDomainService.getAll(organization.getId(), UserRole.ROLE_ORG_ADMIN);

            if (organizationAdmins != null) {
                for (UserDomain organizationAdmin : organizationAdmins) {
                    User user = organizationAdmin.getUser();
                    if (user != null) {
                        users.add(user);
                    }
                }
            }

        }

        return users;
    }

    @Override
    public void deleteLogo(Long organizationId) {
        Organization organization = get(organizationId);
        if (organization != null) {
            CustomBranding customBranding = organization.getCustomBranding();
            if (customBranding != null) {
                AppFile logo = customBranding.getLogo();
                if (logo != null) {
                    customBranding.setLogo(null);
                    appFileService.delete(logo);
                }
            }
        }
    }

    @Override
    public Organization getForGroupId(Long groupId) {
        if (groupId == null || groupId <= 0) {
            log.error("Attempted to retrieve Organization for empty groupId.");
            return null;
        }

        return organizationDao.getForGroupId(groupId);
    }

    private AppFile createLogo(MultipartFile logo, Organization organization) {
        StorageService storageService = getStorageService(organization.getStorageConfigurations().get(0).getId());
        return storageService.save(logo, AppFileType.LOGO.getPathName(), organization.getOrgStorageConfig().getId(), organization.getStorageConfigurations().get(0).getId(), organization.getUuid());
    }

    private StorageService getStorageService(Long storageConfigurationId) {
        return storageServiceFactory.getStorageService(getStorageConfiguration(storageConfigurationId).getStorageType());
    }

    private StorageConfiguration getStorageConfiguration(Long storageConfigurationId) {
        return storageConfigurationService.get(storageConfigurationId);
    }
}
