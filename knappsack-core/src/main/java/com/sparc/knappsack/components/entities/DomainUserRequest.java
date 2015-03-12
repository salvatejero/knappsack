package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.NotifiableType;
import com.sparc.knappsack.enums.Status;

import javax.persistence.*;

@Entity
@Table(name = "DOMAIN_USER_REQUEST")
public class DomainUserRequest extends BaseEntity implements Notifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOMAIN_ID")
    private Domain domain;

    //TODO possibly domain type?

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.DOMAIN_USER_REQUEST;
    }

    @Override
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Domain getDomain() {
        return initializeAndUnproxy(domain);
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
