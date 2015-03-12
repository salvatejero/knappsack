package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.DomainType;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A group is a specific type of domain.  This domain is a subdomain of an organization and owns a group of applications as well as has guest rights
 * to other applications that it does not own.
 * @see Domain
 */
@Entity
@Table(name = "ORG_GROUP")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Group extends Domain {

    private static final long serialVersionUID = -7673360751084836586L;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    @Fetch(FetchMode.JOIN)
    private Organization organization;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "ORG_GROUP_APPLICATION", joinColumns = @JoinColumn(name = "ORG_GROUP_ID"), inverseJoinColumns = @JoinColumn(name = "APPLICATION_ID"))
    @BatchSize(size=30)
    private List<Application> ownedApplications = new ArrayList<Application>();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, targetEntity = ApplicationVersion.class, mappedBy = "guestGroups")
    @BatchSize(size=5)
    private List<ApplicationVersion> guestApplicationVersions = new ArrayList<ApplicationVersion>();

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public DomainType getDomainType() {
        return DomainType.GROUP;
    }

    public List<Application> getOwnedApplications() {
        return ownedApplications;
    }

    @SuppressWarnings("unused")
    public void setOwnedApplications(List<Application> ownedApplications) {
        this.ownedApplications = ownedApplications;
    }

    public List<ApplicationVersion> getGuestApplicationVersions() {
        return guestApplicationVersions;
    }

    public void setGuestApplicationVersions(List<ApplicationVersion> guestApplicationVersions) {
        this.guestApplicationVersions = guestApplicationVersions;
    }

    @Override
    public List<StorageConfiguration> getStorageConfigurations() {
        List<StorageConfiguration> storageConfigurations = new ArrayList<StorageConfiguration>();
        if (organization != null) {
            storageConfigurations.addAll(organization.getStorageConfigurations());
        }

        return storageConfigurations;
    }

    @Override
    public OrgStorageConfig getOrgStorageConfig() {
        OrgStorageConfig orgStorageConfig = null;
        if (organization != null) {
            orgStorageConfig = organization.getOrgStorageConfig();
        }

        return orgStorageConfig;
    }

    @PreRemove
    public void preRemove() {
        setGuestApplicationVersions(null);
        setOrganization(null);
    }
}
