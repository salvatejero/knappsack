package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.models.ManagerChecklist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("managerChecklistService")
public class ManagerChecklistServiceImpl implements ManagerChecklistService {

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Override
    public ManagerChecklist getManagerChecklist(Long organizationId) {
        ManagerChecklist managerChecklist = new ManagerChecklist();
        managerChecklist.setOrganizationId(organizationId);

        Long appCount = organizationService.countOrganizationApps(organizationId);
        boolean hasApps = appCount > 0;
        managerChecklist.setHasApps(hasApps);

        Long appVersionCount = organizationService.countOrganizationAppVersions(organizationId);
        boolean hasAppVersions = appVersionCount > 0;
        managerChecklist.setHasAppVersions(hasAppVersions);

        Long groupCount = organizationService.countOrganizationGroups(organizationId);
        boolean hasGroups = groupCount > 0;
        managerChecklist.setHasGroups(hasGroups);

        Long organizationUserCount = organizationService.countOrganizationUsers(organizationId, true);
        Long organizationInvitationCount = invitationService.countAll(organizationId);
        boolean hasOrganizationUsers = organizationUserCount > 1 || organizationInvitationCount > 0;
        managerChecklist.setHasOrganizationUsers(hasOrganizationUsers);

        if (appCount == 1) {
            Application application = organizationService.getAllOrganizationApplications(organizationId).get(0);
            managerChecklist.setApplicationId(application.getId());
        }

        if (groupCount == 1) {
            Group group = organizationService.get(organizationId).getGroups().get(0);
            managerChecklist.setGroupId(group.getId());
        }
        return managerChecklist;
    }
}
