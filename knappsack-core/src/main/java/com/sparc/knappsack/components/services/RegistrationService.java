package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.models.UserModel;

public interface RegistrationService {
    User registerUser(UserModel userModel, boolean useTemporaryPassword);
}
