package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.events.EventDeliveryWithCompositeFactory;
import com.sparc.knappsack.components.events.composits.EventComposite;
import com.sparc.knappsack.components.events.composits.EventDeliveryWithComposite;
import com.sparc.knappsack.components.events.composits.UserPasswordResetComposite;
import com.sparc.knappsack.enums.EventType;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service("userControllerService")
public class UserControllerServiceImpl implements UserControllerService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("eventDeliveryWithCompositeFactory")
    @Autowired(required = true)
    private EventDeliveryWithCompositeFactory eventDeliveryWithCompositeFactory;

    @Override
    public boolean resetPassword(User user) {
        boolean success = false;
        if (user != null) {
            String newPassword = RandomStringUtils.randomAlphanumeric(10);
            success = changePassword(user, newPassword, true);
        }

        return success;
    }

    @Override
    public boolean changePassword(User user, String password, boolean isTempPassword) {
        boolean success = false;

        if (user != null && StringUtils.hasText(password)) {
            String oldPassword = user.getPassword();
            userService.changePassword(user, password, isTempPassword);

            User updatedUser = userService.getByEmail(user.getEmail());

            if (updatedUser != null && StringUtils.hasText(updatedUser.getPassword()) && !updatedUser.getPassword().equals(oldPassword)) {
                userService.updateSecurityContext(updatedUser);

                success = true;

                if (isTempPassword) {
                    EventComposite composite = new UserPasswordResetComposite(password);
                    EventDeliveryWithComposite deliveryMechanism = eventDeliveryWithCompositeFactory.getEventDelivery(EventType.USER_PASSWORD_RESET);
                    if (deliveryMechanism != null) {
                        boolean notificationSent = deliveryMechanism.sendNotifications(updatedUser, composite);
                        if (!notificationSent) {
                            log.info(String.format("Password reset email not sent to: %s", updatedUser.getEmail()));
                        }
                    }
                }

            } else {
                log.info(String.format("User password not reset with email address during password reset: %s", user.getEmail()));
            }
        }

        return success;
    }
}
