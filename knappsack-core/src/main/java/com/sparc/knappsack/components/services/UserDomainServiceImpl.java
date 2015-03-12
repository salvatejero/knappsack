package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.UserDomainDao;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.entities.UserDomain;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.UserDomainModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Transactional( propagation = Propagation.REQUIRED )
@Service("userDomainService")
public class UserDomainServiceImpl implements UserDomainService {

    @Qualifier("userDomainDao")
    @Autowired(required = true)
    private UserDomainDao userDomainDao;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("roleService")
    @Autowired(required = true)
    private RoleService roleService;

    @Override
    public UserDomain get(Long id) {
        return userDomainDao.get(id);
    }

    @Override
    public void delete(Long id) {
        delete(get(id));
    }

    @Override
    public void update(UserDomain userDomain) {
        userDomainDao.update(userDomain);
    }

    @Override
    public void add(UserDomain userDomain) {
        userDomainDao.add(userDomain);
    }

    @Override
    public UserDomain get(User user, Long domainId, UserRole userRole) {
        return userDomainDao.getUserDomain(user, domainId, userRole);
    }

    @Override
    public UserDomain get(User user, Long domainId) {
        UserDomain userDomain = null;
        if (user != null && domainId != null && domainId > 0) {
            userDomain = userDomainDao.getUserDomain(user, domainId);
        }
        return userDomain;
    }

    @Override
    public List<UserDomain> getAll(Long domainId) {
        List<UserDomain> userDomains = new ArrayList<UserDomain>();
        if (domainId != null && domainId > 0) {
            userDomains.addAll(userDomainDao.getUserDomainsForDomain(domainId));
        }
        return userDomains;
    }

    @Override
    public List<UserDomain> getAll(Long domainId, UserRole... userRoles) {
        List<UserDomain> userDomains = new ArrayList<UserDomain>();

        if (userRoles != null && userRoles.length > 0) {
            List<UserDomain> returnedUserDomains = userDomainDao.getUserDomainsForDomainAndRoles(domainId, userRoles);
            if (returnedUserDomains != null) {
                userDomains.addAll(returnedUserDomains);
            }
        }

        return userDomains;
    }

    @Override
    public List<UserDomain> getAll(Long domainId, DomainType domainType, UserRole... userRoles) {
        List<UserDomain> userDomains = new ArrayList<UserDomain>();

        if (domainType != null && userRoles != null && userRoles.length > 0) {
            List<UserDomain> returnedUserDomains = getAll(domainId, userRoles);
            if (returnedUserDomains != null) {
                for (UserDomain userDomain : returnedUserDomains) {
                    Domain domain = userDomain.getDomain();
                    if (domain != null && domainType.equals(domain.getDomainType())) {
                        userDomains.add(userDomain);
                    }
                }
            }
        }

        return userDomains;
    }

    @Override
    public List<UserDomain> getUserDomainsForEmailsAndDomains(List<String> emails, List<Long> domainIds) {
        if (CollectionUtils.isEmpty(emails) || CollectionUtils.isEmpty(domainIds)) {
            return new ArrayList<UserDomain>();
        }

        List<UserDomain> userDomains = userDomainDao.getUserDomainsForEmailsAndDomains(emails, domainIds);
        if (CollectionUtils.isEmpty(userDomains)) {
            return new ArrayList<UserDomain>();
        } else {
            return userDomains;
        }
    }

    @Override
    public void removeUserDomainFromDomain(Long domainId, Long userId) {
        if (domainId != null && domainId > 0 && userId != null && userId > 0) {
            User user = userService.get(userId);
            if (user != null && user.getUserDomains() != null) {
                UserDomain userDomain = userDomainDao.getUserDomain(user, domainId);

                if (userDomain != null) {
                    if (user.getUserDomains().contains(userDomain)) {
                        user.getUserDomains().remove(userDomain);
                    }

                    userDomainDao.delete(userDomain);
                }
            }
        }
    }

    @Override
    public void updateUserDomainRole(Long userId, Long domainId, UserRole userRole) {
        User user = userService.get(userId);
        UserDomain userDomain = get(user, domainId);

        if (userDomain != null) {
            userDomain.setRole(roleService.getRoleByAuthority(userRole.toString()));

            update(userDomain);
        }
    }

    @Override
    public void removeAllFromDomain(Long domainId) {
        if (domainId != null && domainId > 0) {
            userDomainDao.removeAllFromDomain(domainId);
        }
    }

    @Override
    public UserDomainModel createUserDomainModel(UserDomain userDomain) {
        UserDomainModel model = null;
        if (userDomain != null && userDomain.getDomain() != null) {
            model = new UserDomainModel();
            model.setId(userDomain.getId());
            model.setDomainId(userDomain.getDomain().getId());
            model.setDomainType(userDomain.getDomain().getDomainType());
            model.setUser(userService.createUsermodel(userDomain.getUser()));
            model.setUserRole(userDomain.getRole().getUserRole());
        }
        return model;
    }

    @Override
    public long countDomains(User user, DomainType domainType, UserRole userRole) {
        return userDomainDao.countDomains(user, userRole);
    }

    @Override
    public void delete(UserDomain userDomain) {
        if (userDomain != null) {
            userDomainDao.delete(userDomain);
        }
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
