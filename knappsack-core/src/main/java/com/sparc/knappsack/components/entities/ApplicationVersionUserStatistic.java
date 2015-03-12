package com.sparc.knappsack.components.entities;


import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(name = "APPLICATION_VERSION_USER_STATISTIC")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ApplicationVersionUserStatistic extends BaseEntity {

    private static final long serialVersionUID = -8010813761341730769L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "APPLICATION_VERSION_ID", nullable = false, referencedColumnName = "ID")
    private ApplicationVersion applicationVersion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, referencedColumnName = "ID")
    private User user;

    @Column(name = "REMOTE_ADDRESS")
    private String remoteAddress;

    @Column(name = "USER_AGENT")
    private String userAgent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApplicationVersion getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(ApplicationVersion applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ApplicationVersionUserStatistic");
        sb.append("{id=").append(id);
        sb.append(", applicationVersion=").append(applicationVersion);
        sb.append(", user=").append(user);
        sb.append(", remoteAddress='").append(remoteAddress).append('\'');
        sb.append(", userAgent='").append(userAgent).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
