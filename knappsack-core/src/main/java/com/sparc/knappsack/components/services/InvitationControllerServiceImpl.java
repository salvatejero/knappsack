package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.BatchInvitationForm;
import com.sparc.knappsack.forms.InvitationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service("invitationControllerService")
public class InvitationControllerServiceImpl implements InvitationControllerService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Override
    public boolean inviteUser(InvitationForm invitationForm, boolean deleteInvitationsOnSendError) {
        boolean userInvited = false;
        User user = userService.getUserFromSecurityContext();
        if (invitationForm != null && user != null) {

            List<Invitation> createdInvitations = invitationService.createInvitations(invitationForm);
            userInvited = sendInvitation(createdInvitations, deleteInvitationsOnSendError);
        }

        return userInvited;
    }

    @Override
    public boolean inviteBatchUsers(BatchInvitationForm batchInvitationForm, boolean deleteInvitationOnSendError) {
        boolean usersInvited = false;
        User user = userService.getUserFromSecurityContext();
        if (batchInvitationForm != null && user != null) {
            List<Invitation> createdInvitations = invitationService.createInvitations(batchInvitationForm);
            usersInvited = sendInvitation(createdInvitations, deleteInvitationOnSendError);
        }

        return usersInvited;
    }

    @Override
    public boolean inviteUserToDomain(String email, Long domainId, UserRole userRole, boolean deleteInvitationOnSendError) {
        if (StringUtils.hasText(email) && domainId != null && domainId > 0 && userRole != null) {
            List<Invitation> createdInvitations = new ArrayList<Invitation>();
            createdInvitations.add(invitationService.createInvitation(StringUtils.trimAllWhitespace(email), userRole, domainId));
            return sendInvitation(createdInvitations, deleteInvitationOnSendError);
        }

        return false;
    }

    @Override
    public boolean sendInvitation(List<Invitation> invitations, boolean deleteOnError) {
        boolean invitationSent = false;
        if (!CollectionUtils.isEmpty(invitations)) {
            EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.USER_INVITE);
            try {
                invitationSent = deliveryMechanism.sendNotifications(invitations);
            } catch (MailException e) {
                log.error("Error during invitations notification.", e);
                if (deleteOnError) {
                    for (Invitation invitation : invitations) {
                        invitationService.delete(invitation.getId());
                    }
                }
            }
        }

        return invitationSent;
    }

}
