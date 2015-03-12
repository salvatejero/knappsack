package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.NotifiableType;
import com.sparc.knappsack.enums.UserRole;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

/**
 * A User is a person in the Knappsack system. This entity contains relevant information pertaining to the person.
 * @see UserDetails
 */
@Entity
@Table(name = "USER")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends BaseEntity implements UserDetails, Notifiable {

    private static final long serialVersionUID = 1690650481252062307L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USERNAME", unique = true)
    private String username;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "EMAIL")
    private String email;

    @ElementCollection(targetClass = java.lang.String.class, fetch = FetchType.EAGER)
    @MapKeyClass(java.lang.String.class)
    @CollectionTable(name = "USER_OPENIDIDENTIFIER", joinColumns = @JoinColumn(name = "USER_ID"))
    @Column(name = "OPENIDIDENTIFIER")
    @BatchSize(size = 10)
    private Set<String> openIdIdentifiers = new HashSet<String>();

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "FULL_NAME")
    private String fullName;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(name = "USER_ROLE", joinColumns = @JoinColumn(name = "USER_ID"), inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
    private List<Role> roles = new ArrayList<Role>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    @BatchSize(size = 5)
    private List<UserDomain> userDomains = new ArrayList<UserDomain>();

    @Column(name = "ACTIVATED")
    private boolean activated = false;

    @Column(name = "ACTIVATION_CODE")
    private String activationCode;

    @Column(name = "PASSWORD_EXPIRED")
    private boolean passwordExpired = false;

    @OneToOne(cascade = CascadeType.REFRESH, orphanRemoval = false)
    @JoinColumn(name = "ACTIVE_ORGANIZATION_ID")
    @LazyToOne(value = LazyToOneOption.PROXY)
    private Organization activeOrganization;

    public User() {
        this.username = "";
        this.password = "";
        this.email = "";
        this.firstName = "";
        this.lastName = "";
        this.fullName = "";
        activationCode = UUID.randomUUID().toString();
    }

    public User(String username, String password, String email, String firstName, String lastName, Collection<? extends Role> authorities) {
        this.username = username.toLowerCase().trim();
        this.password = password;
        this.email = email.toLowerCase().trim();
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;

        this.openIdIdentifiers = new HashSet<String>();

        roles.addAll(authorities);

        activationCode = UUID.randomUUID().toString();
    }

    @Transient
    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.USER;
    }

    public Long getId() {
        return id;
    }

    public List<Role> getRoles() {
        return roles;
    }

    @SuppressWarnings("unused")
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @Transient
    public Collection<GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        for (Role role : getRoles())
        {
            authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
        }

        return authorities;
    }

    public String getEmail() {
        return email;
    }

    @SuppressWarnings("unused")
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public Set<String> getOpenIdIdentifiers() {
        return openIdIdentifiers;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase().trim();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase().trim();
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @SuppressWarnings("unused")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @SuppressWarnings("unused")
    public void setOpenIdIdentifiers(Set<String> openIdIdentifiers) {
        this.openIdIdentifiers = openIdIdentifiers;
    }

    public boolean isAnyAdmin() {
        return isSystemAdmin() || isGroupAdmin() || isOrganizationAdmin();
    }
    
    public boolean isSystemAdmin() {
        for (Role role : roles) {
            if(UserRole.ROLE_ADMIN.toString().equals(role.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public boolean isActiveOrganizationAdmin() {
        if(isSystemAdmin()) {
            return true;
        }

        if(activeOrganization != null) {
            for (UserDomain userDomain : userDomains) {
                if (DomainType.ORGANIZATION.equals(userDomain.getDomain().getDomainType())
                        && userDomain.getDomain().getId().equals(activeOrganization.getId())
                        && UserRole.ROLE_ORG_ADMIN.toString().equals(userDomain.getRole().getAuthority())) {
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unused")
    public boolean isActiveOrganizationGroupAdmin() {
        if (isSystemAdmin()) {
            return true;
        }

        if (activeOrganization != null) {
            for (UserDomain userDomain : getUserDomains()) {
                if (DomainType.GROUP.equals(userDomain.getDomain().getDomainType())
                        && ((Group) userDomain.getDomain()).getOrganization() != null
                        && ((Group) userDomain.getDomain()).getOrganization().getId().equals(activeOrganization.getId())
                        && UserRole.ROLE_GROUP_ADMIN.equals(userDomain.getRole().getUserRole())) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isOrganizationAdmin() {
        for (UserDomain userDomain : userDomains) {
            if (DomainType.ORGANIZATION.equals(userDomain.getDomain().getDomainType()) && UserRole.ROLE_ORG_ADMIN.toString().equals(userDomain.getRole().getAuthority())) {
                return true;
            }
        }

        return false;
    }

    public boolean isGroupAdmin() {
        for (UserDomain userDomain : userDomains) {
            if (DomainType.GROUP.equals(userDomain.getDomain().getDomainType()) && UserRole.ROLE_GROUP_ADMIN.toString().equals(userDomain.getRole().getAuthority())) {
                return true;
            }
        }

        return false;
    }

    public boolean isSystemOrOrganizationAdmin() {
        return isSystemAdmin() || isOrganizationAdmin();
    }

    public boolean isOrganizationOrGroupAdmin() {
        return isGroupAdmin() || isOrganizationAdmin();
    }

    @SuppressWarnings("unused")
    public boolean isOrganizationAdminWithResignerEnabled() {
        if(isSystemAdmin()) {
            return true;
        }

        if(isActiveOrganizationAdmin()) {
            return activeOrganization.getDomainConfiguration().isApplicationResignerEnabled();

        }

        return isSystemAdmin();
    }

    public List<UserDomain> getUserDomains() {
        if (userDomains == null) {
            userDomains = new ArrayList<UserDomain>();
        }
        return userDomains;
    }

    @SuppressWarnings("unused")
    public void setUserDomains(List<UserDomain> userDomains) {
        this.userDomains = userDomains;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getActivationCode() {
        return activationCode;
    }

    @SuppressWarnings("unused")
    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }

    public Organization getActiveOrganization() {
        return activeOrganization;
    }

    public void setActiveOrganization(Organization activeOrganization) {
        this.activeOrganization = activeOrganization;
    }
}

