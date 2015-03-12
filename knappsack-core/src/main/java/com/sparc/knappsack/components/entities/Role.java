package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.UserRole;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

/**
 * A Role is the permission a User has as pertains to Knappsack or a domain.
 * @see UserDomain
 */
@Entity
@Table(name = "ROLE")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Role extends BaseEntity implements GrantedAuthority {

    private static final long serialVersionUID = -117212611936641518L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "AUTHORITY")
    private String authority;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Transient
    public UserRole getUserRole() {
        return UserRole.valueOf(this.authority);
    }
}