package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.BatchInvitationForm;
import com.sparc.knappsack.forms.InvitationForm;

import java.util.List;

public interface InvitationControllerService {

    public boolean inviteUser(InvitationForm invitationForm, boolean deleteInvitationsOnSendError);

    public boolean inviteBatchUsers(BatchInvitationForm batchInvitationForm, boolean deleteInvitationOnSendError);

    public boolean inviteUserToDomain(String email, Long domainId, UserRole userRole, boolean deleteInvitationOnSendError);

    public boolean sendInvitation(List<Invitation> invitations, boolean deleteOnError);

}
