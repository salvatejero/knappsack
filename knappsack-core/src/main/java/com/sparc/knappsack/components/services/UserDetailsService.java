package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.UserDetailsDao;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.exceptions.OpenIDUserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.stereotype.Service;

//@Transactional
@Service("userDetailsService")
public class UserDetailsService implements AuthenticationUserDetailsService<OpenIDAuthenticationToken>, org.springframework.security.core.userdetails.UserDetailsService {

    @Qualifier("userDetailsDao")
    @Autowired(required = true)
    private UserDetailsDao userDetailsDao;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    public User loadUserByUsername(String userName) throws UsernameNotFoundException, DisabledException {
        User user = userDetailsDao.findByEmail(userName);
        if (user == null) {
            throw new UsernameNotFoundException("User not found for userName: " + userName);
        } else {
            if (!user.isEnabled()) {
                throw new DisabledException("User is disabled");
            }
            userService.setDefaultActiveOrganization(user);
            return user;
        }
    }

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserDetails(OpenIDAuthenticationToken token) throws UsernameNotFoundException {
        User existingUser = null;
        for (OpenIDAttribute attribute : token.getAttributes()) {
            if ("email".equals(attribute.getName())) {
                existingUser = userDetailsDao.findByEmail(attribute.getValues().get(0).toLowerCase());
                break;
            }
        }
        if (existingUser == null) {
            throw new OpenIDUserNotFoundException("User not found for OpenID: " + token.getName(), token);
        } else {
            existingUser.getOpenIdIdentifiers().add(token.getIdentityUrl());
            userService.setDefaultActiveOrganization(existingUser);
            userDetailsDao.update(existingUser);
        }

        return existingUser;
    }
}