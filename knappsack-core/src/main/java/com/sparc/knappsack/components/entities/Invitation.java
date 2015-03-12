package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.NotifiableType;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * An invitation represents a request by an admin of a domain (group, organization, etc...) to a user for access to the admin's domain.
 */
@Entity
@Table(name = "INVITATION", uniqueConstraints = {@UniqueConstraint(name = "UNQ_INVITATION", columnNames={"EMAIL", "DOMAIN_ID", "ROLE_ID"})})
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Invitation extends BaseEntity implements Notifiable {

    private static final long serialVersionUID = -1170543161710859798L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "EMAIL")
    private String email;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "DOMAIN_ID", nullable = false)
    private Domain domain;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = false)
    @JoinColumn(name = "ROLE_ID", nullable = false)
    private Role role;

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Domain getDomain() {
        return initializeAndUnproxy(domain);
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Transient
    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.INVITATION;
    }
}
