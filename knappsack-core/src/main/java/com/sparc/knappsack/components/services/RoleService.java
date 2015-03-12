package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Role;

public interface RoleService {
    Role getRoleByAuthority(String authority);
}
