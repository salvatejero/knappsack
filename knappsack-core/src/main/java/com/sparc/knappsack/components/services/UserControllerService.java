package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.User;

public interface UserControllerService {

    public boolean resetPassword(User user);

    public boolean changePassword(User user, String password, boolean isTempPassword);

}
