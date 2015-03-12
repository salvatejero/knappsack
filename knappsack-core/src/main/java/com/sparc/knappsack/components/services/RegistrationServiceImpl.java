package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.UserDetailsDao;
import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.Role;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional( propagation = Propagation.REQUIRED )
@Service("registrationService")
public class RegistrationServiceImpl implements RegistrationService {

    @Qualifier("userDetailsDao")
    @Autowired(required = true)
    private UserDetailsDao userDetailsDao;

    @Qualifier("roleService")
    @Autowired(required = true)
    private RoleService roleService;

    @Qualifier("passwordEncoder")
    @Autowired(required = true)
    private PasswordEncoder passwordEncoder;

    @Autowired(required = true)
    private UserService userService;

    @Autowired(required = true)
    private InvitationService invitationService;

    @Override
    public User registerUser(UserModel userModel, boolean useTemporaryPassword) {
        User user = null;
        if (userModel != null) {
            List<Role> userRoles = new ArrayList<Role>();
            Role userRole = roleService.getRoleByAuthority(UserRole.ROLE_USER.toString());
            userRoles.add(userRole);

            user = new User(userModel.getEmail(), passwordEncoder.encodePassword(userModel.getPassword(), userModel.getEmail().toLowerCase().trim()), userModel.getEmail(), userModel.getFirstName(), userModel.getLastName(), userRoles);
            user.setPasswordExpired(useTemporaryPassword);
            //If this is the first user in the application, set them as a Knappsack administrator
            if (userService.countAll() == 0) {
                setAdminRole(user);
            }
            userDetailsDao.add(user);
            addUserToInvitedDomains(user);
        }

        return user;
    }

    private void addUserToInvitedDomains(User user) {

        String email = user.getEmail();

        List<Invitation> invitations = invitationService.getByEmail(email);
        for (Invitation invitation : invitations) {
            if(DomainType.GROUP.equals(invitation.getDomain().getDomainType())) {
                userService.addUserToGroup(user, invitation.getDomain().getId(), UserRole.valueOf(invitation.getRole().getAuthority()));
            } else if(DomainType.ORGANIZATION.equals(invitation.getDomain().getDomainType())) {
                userService.addUserToOrganization(user, invitation.getDomain().getId(), UserRole.valueOf(invitation.getRole().getAuthority()));
            }
        }

        for (Invitation invitation : invitations) {
            invitationService.delete(invitation.getId());
        }
    }

    private void setAdminRole(User user) {
        Role role = roleService.getRoleByAuthority(UserRole.ROLE_ADMIN.toString());
        user.getRoles().add(role);
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return UserRole.ROLE_ADMIN.toString();
            }
        };
        user.getAuthorities().add(grantedAuthority);
    }
}
