package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.forms.GroupForm;
import com.sparc.knappsack.models.GroupModel;

import java.util.List;

public interface GroupService extends EntityService<Group>, DomainEntityService<Group> {
    void save(Group group);

    Group get(String name, Long organizationId);

    void mapGroupToGroupForm(Group group, GroupForm groupForm);

    Group createGroup(GroupForm groupForm);

    void editGroup(GroupForm groupForm);

    List<Group> getAll();

    Group getByAccessCode(String accessCode);

    List<Group> getGuestGroups(ApplicationVersion applicationVersion);

    Group getOwnedGroup(Application application);

    void removeUserFromGroup(Long groupId, Long userId);

    long getTotalUsers(Group group);

    long getTotalPendingInvitations(Group group);

    long getTotalApplications(Group group);

    long getTotalApplicationVersions(Group group);

    double getTotalMegabyteStorageAmount(Group group);

    /**
     * @param group Group - check to see if this group has reached the maximum number of applications allowed.
     * @return boolean true if the group is at the maximum number of applications allowed.
     */
    boolean isApplicationLimit(Group group);

    GroupModel createGroupModel(Long groupId);

    GroupModel createGroupModel(Group group);

    GroupModel createGroupModelWithOrganization(Group group, boolean includeOrgStorageConfig, boolean includeExternalData);
}
