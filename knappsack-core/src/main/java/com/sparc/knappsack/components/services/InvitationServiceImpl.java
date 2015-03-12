package com.sparc.knappsack.components.services;

import com.csvreader.CsvReader;
import com.sparc.knappsack.components.dao.InvitationDao;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.Role;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.BatchInvitationForm;
import com.sparc.knappsack.forms.InvitationForm;
import com.sparc.knappsack.forms.InviteeForm;
import com.sparc.knappsack.models.Contact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Transactional( propagation = Propagation.REQUIRED )
@Service("invitationService")
public class InvitationServiceImpl implements InvitationService {

    private static final Logger log = LoggerFactory.getLogger(InvitationServiceImpl.class);

    @Qualifier("invitationDao")
    @Autowired(required = true)
    private InvitationDao invitationDao;

    @Qualifier("roleService")
    @Autowired(required = true)
    private RoleService roleService;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Override
    public Invitation get(Long id) {
        return invitationDao.get(id);
    }

    @Override
    public void add(Invitation invitation) {
        invitationDao.add(invitation);
    }

    @Override
    public void update(Invitation invitation) {
        invitationDao.update(invitation);
    }

    @Override
    public List<Invitation> getByEmail(String email) {
        return invitationDao.getByEmail(email);
    }

    @Override
    public Invitation get(String email, Domain domain, Role role) {
        return invitationDao.get(email, domain, role);
    }

    @Override
    public void delete(Long id) {
        invitationDao.delete(get(id));
    }

    @Override
    public List<Invitation> getAll(Long domainId) {
        List<Invitation> invitations = new ArrayList<Invitation>();

        invitations.addAll(invitationDao.getAllForDomain(domainId));

        return invitations;
    }

    @Override
    public long countAll(Long domainId) {
        return invitationDao.countAll(domainId);
    }

    @Override
    public long countAllForOrganizationIncludingGroups(Long organizationId) {
        if (organizationId == null || organizationId <= 0) {
            return 0;
        }

        return invitationDao.countAllForOrganizationIncludingGroups(organizationId);
    }

    @Override
    public List<Invitation> getAll(String email, Long domainId) {
        List<Invitation> invitations = new ArrayList<Invitation>();

        invitations.addAll(invitationDao.getAllForEmailAndDomain(email, domainId));

        return invitations;
    }

    @Override
    public long countEmailsWithoutInvitationsForOrganization(Set<String> emails, Long organizationId, boolean includeGroups) {

        // Only attempt if emails and organization id are not empty
        if (CollectionUtils.isEmpty(emails) || organizationId == null || organizationId <= 0) {
            return 0;
        }

        return invitationDao.countEmailsWithoutInvitationsForOrganization(emails, organizationId, includeGroups);
    }

    @Override
    public List<Invitation> getAllForEmailsAndDomains(List<String> emails, List<Long> domainIds) {
        if (CollectionUtils.isEmpty(emails) || CollectionUtils.isEmpty(domainIds)) {
            return new ArrayList<Invitation>();
        }

        Set<Invitation> invitations = new HashSet<Invitation>();
        invitations.addAll(invitationDao.getAllForEmailsAndDomains(emails, domainIds));

        return new ArrayList<Invitation>(invitations);
    }

    @Override
    public Invitation createInvitation(InviteeForm inviteeForm, Long domainID) {
        Invitation invitation = null;
        if(inviteeForm.getEmail() != null && !inviteeForm.getEmail().isEmpty() && !inviteeForm.isDelete()) {
            invitation = setupUserInvites(inviteeForm.getEmail(), inviteeForm.getUserRole(), domainID);

            add(invitation);
        }
        return invitation;
    }

    @Override
    public Invitation createInvitation(String inviteeEmail, UserRole userRole, Long domainId) {
        Invitation invitation = null;
        if (StringUtils.hasText(inviteeEmail) && userRole != null && domainId != null && domainId > 0) {
            invitation = setupUserInvites(inviteeEmail.trim(), userRole, domainId);

            add(invitation);
        }
        return invitation;
    }

    @Override
    public List<Invitation> createInvitations(InvitationForm invitationForm) {
        Assert.notNull(invitationForm, "InvitationForm cannot be null");

        List<Invitation> invitations = new ArrayList<Invitation>();

        User user = userService.getUserFromSecurityContext();
        if (user != null) {

            // Create Organization invitation if necessary
            if (invitationForm.getOrganizationUserRole() != null && !UserRole.ROLE_ORG_GUEST.equals(invitationForm.getOrganizationUserRole())) {
                invitations.add(createInvitation(StringUtils.trimAllWhitespace(invitationForm.getEmail()), invitationForm.getOrganizationUserRole(), user.getActiveOrganization().getId()));
            }

            if (!CollectionUtils.isEmpty(invitationForm.getGroupIds()) && invitationForm.getGroupUserRole() != null) {
                for (Long groupId : invitationForm.getGroupIds()) {
                    invitations.add(createInvitation(StringUtils.trimAllWhitespace(invitationForm.getEmail()), invitationForm.getGroupUserRole(), groupId));
                }
            }
        }

        return invitations;
    }

    @Override
    public List<Invitation> createInvitations(BatchInvitationForm batchInvitationForm) {
        Assert.notNull(batchInvitationForm, "BatchInvitationForm cannot be null");

        List<Invitation> invitations = new ArrayList<Invitation>();

        User user = userService.getUserFromSecurityContext();
        if (user != null) {
            for (Contact contact : batchInvitationForm.getContacts()) {
                // Create Organization invitation if necessary
                if (batchInvitationForm.getOrganizationUserRole() != null && !UserRole.ROLE_ORG_GUEST.equals(batchInvitationForm.getOrganizationUserRole())) {
                    invitations.add(createInvitation(StringUtils.trimAllWhitespace(contact.getEmail()), batchInvitationForm.getOrganizationUserRole(), user.getActiveOrganization().getId()));
                }

                if (!CollectionUtils.isEmpty(batchInvitationForm.getGroupIds())) {
                    for (Long groupId : batchInvitationForm.getGroupIds()) {
                        invitations.add(createInvitation(StringUtils.trimAllWhitespace(contact.getEmail()), UserRole.ROLE_GROUP_USER, groupId));
                    }
                }
            }
        }

        return invitations;
    }

    @Override
    public long deleteAll(Long domainId) {
        long numDeleted = 0;
        Domain domain = domainService.get(domainId);
        if (domain != null) {
            numDeleted = deleteAll(domain);
        }
        return numDeleted;
    }

    @Override
    public long deleteAll(Domain domain) {
        long numDeleted = 0;
        if (domain != null) {
            numDeleted = invitationDao.deleteAllForDomain(domain);
        }
        return numDeleted;
    }

    @Override
    public void deleteInvitation(Long invitationId) {
        //Only delete invitation if a user already exists.
        Invitation invitation = get(invitationId);
        if (invitation != null) {
            User user = userService.getByEmail(invitation.getEmail());
            if (user != null) {
                invitationDao.delete(invitation);
            }
        }
    }

    private Invitation setupUserInvites(String email, UserRole userRole, Long domainId) {

        Role role = roleService.getRoleByAuthority(userRole.name());

        Invitation invitation = new Invitation();
        invitation.setDomain(domainService.get(domainId));
        invitation.setEmail(email.toLowerCase().trim());
        invitation.setRole(role);

        return invitation;
    }

    public List<Contact> parseContactsGoogle(MultipartFile contactsFile) {
        List<Contact> contactsList = new ArrayList<Contact>();
        if(contactsFile == null) {
            return contactsList;
        }

        try {
            Reader reader = new InputStreamReader(contactsFile.getInputStream());
            CsvReader contacts = new CsvReader(reader);
            contacts.readHeaders();
            while (contacts.readRecord()) {
                String name = contacts.get("Name");
                String email = contacts.get("E-mail 1 - Value");
                if(!StringUtils.hasText(email)) {
                    continue;
                }
                Contact contact = new Contact();
                contact.setEmail(StringUtils.trimAllWhitespace(email.toLowerCase()));
                contact.setName(StringUtils.trimTrailingWhitespace(StringUtils.trimLeadingWhitespace(name)));
                contactsList.add(contact);
            }
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException caught attempting to parse an Google contacts CSV file.", e);
        } catch (IOException e) {
            log.error("IOException caught attempting to parse an Google contacts CSV file.", e);
        }

        return contactsList;
    }

    public List<Contact> parseContactsOutlook(MultipartFile contactsFile) {
        List<Contact> contactList = new ArrayList<Contact>();
        if(contactsFile == null) {
            return contactList;
        }

        try {
            Reader reader = new InputStreamReader(contactsFile.getInputStream());
            CsvReader contacts = new CsvReader(reader);
            contacts.readHeaders();
            while (contacts.readRecord()) {
                String email = contacts.get("E-mail Address");
                if(!StringUtils.hasText(email)) {
                    continue;
                }
                String firstName = contacts.get("First Name");
                String lastName = contacts.get("Last Name");
                String fullName = firstName + " " + lastName;

                Contact contact = new Contact();
                contact.setEmail(StringUtils.trimAllWhitespace(email).toLowerCase());
                contact.setName(StringUtils.trimTrailingWhitespace(StringUtils.trimLeadingWhitespace(fullName)));
                contactList.add(contact);
            }
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException caught attempting to parse an Outlook contacts CSV file.", e);
        } catch (IOException e) {
            log.error("IOException caught attempting to parse an Outlook contacts CSV file.", e);
        }

        return contactList;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
