package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.RoleDao;
import com.sparc.knappsack.components.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional( propagation = Propagation.REQUIRED )
@Service("roleService")
public class RoleServiceImpl implements RoleService {

    @Qualifier("roleDaoImpl")
    @Autowired(required = true)
    private RoleDao roleDao;
    
    @Override
    public Role getRoleByAuthority(String authority) {
        Role role = roleDao.getByAuthority(authority);
        if(role == null) {
            role = new Role();
            role.setAuthority(authority);
            roleDao.add(role);
        }
        return role;
    }
}
