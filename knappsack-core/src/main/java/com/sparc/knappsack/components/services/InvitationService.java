package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.Role;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.BatchInvitationForm;
import com.sparc.knappsack.forms.InvitationForm;
import com.sparc.knappsack.forms.InviteeForm;
import com.sparc.knappsack.models.Contact;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface InvitationService extends EntityService<Invitation> {

    Invitation get(String email, Domain domain, Role role);

    List<Invitation> getByEmail(String email);

    Invitation createInvitation(InviteeForm inviteeForm, Long domainID);

    Invitation createInvitation(String inviteeEmail, UserRole userRole, Long domainId);

    List<Invitation> createInvitations(InvitationForm invitationForm);

    List<Invitation> createInvitations(BatchInvitationForm batchInvitationForm);

    /**
     * @param domainId Long - the ID of the domain that the user is invited to
     * @return List of all Invitations belonging to this specific domain
     */
    List<Invitation> getAll(Long domainId);

    /**
     * @param domainId Long - the ID of the domain that the user is invited to
     * @return long - a count of all the invitations currently in the system for this domain
     */
    long countAll(Long domainId);

    /**
     * @param organizationId Long - the ID of the organization to search on
     * @return long - a count of all invitations currently in the system for the organization and child groups
     */
    long countAllForOrganizationIncludingGroups(Long organizationId);

    /**
     * @param emails Emails to search on
     * @param organizationId ID of organization to search on
     * @param includeGroups Whether or not invitations for groups of the Organization should be includes
     * @return The number of emails in the list provided which do NOT have existing invitations
     */
    long countEmailsWithoutInvitationsForOrganization(Set<String> emails, Long organizationId, boolean includeGroups);

    List<Invitation> getAllForEmailsAndDomains(List<String> emails, List<Long> domainIds);

    /**
     * @param email String - the email address of the invited user
     * @param domainId Long - the ID of the domain that the user is invited to
     * @return List of Invitations - the user may have multiple invitations for the same domain as long as the roles are different
     */
    List<Invitation> getAll(String email, Long domainId);

    List<Contact> parseContactsGoogle(MultipartFile contactsFile);

    List<Contact> parseContactsOutlook(MultipartFile contactsFile);

    long deleteAll(Long domainId);

    long deleteAll(Domain domain);

    /**
     * Note: The invitation will only be deleted if the user already exists in Knappsack.  Deleting an invitation
     * for a un-registered user would not allow the user to register since registration is invitation only.
     *
     * @param invitationId Invitation ID to be deleted.
     */
    void deleteInvitation(Long invitationId);

}
