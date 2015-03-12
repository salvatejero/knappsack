package com.sparc.knappsack.components.entities;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * A UserDomain entity is the permission a User has to a specific Domain.  A User may belong to many different domains and have different
 * roles for each domain.
 *
 * @see Domain
 * @see User
 */
@Entity
@Table(name = "USER_DOMAIN",
        uniqueConstraints = {@UniqueConstraint(columnNames={"DOMAIN_ID", "ROLE_ID", "USER_ID"})}
)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserDomain extends BaseEntity {

    private static final long serialVersionUID = 71727224831146511L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "DOMAIN_ID", nullable = false)
    private Domain domain;

    @ManyToOne(cascade = CascadeType.REFRESH, optional = false)
    @JoinColumn(name = "ROLE_ID", nullable = false)
    private Role role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Domain getDomain() {
        return initializeAndUnproxy(domain);
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

//    public DomainType getDomainType() {
//        return domainType;
//    }
//
//    public void setDomainType(DomainType domainType) {
//        this.domainType = domainType;
//    }
//
//    public Long getDomainId() {
//        return domainId;
//    }
//
//    public void setDomainId(Long domainId) {
//        this.domainId = domainId;
//    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
