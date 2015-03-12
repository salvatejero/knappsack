package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.NotifiableType;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * An Organization is a specific type of Domain.  This domain is the root of other subdomains.
 * @see Domain
 */
@Entity
@Table(name = "ORGANIZATION")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Organization extends Domain implements Notifiable {

    private static final long serialVersionUID = 396567098678320561L;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization", orphanRemoval = true)
    private List<Category> categories = new ArrayList<Category>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization", orphanRemoval = true)
    @BatchSize(size = 2)
    private List<Group> groups = new ArrayList<Group>();

    //This is really OneToOne, @LazyToOne wasn't working, so using @OneToMany is a workaround.
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization", orphanRemoval = true)
    private List<OrgStorageConfig> orgStorageConfigs = new ArrayList<OrgStorageConfig>();

    @OneToOne(cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true, optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOM_BRANDING_ID", nullable = true)
//    @LazyToOne(LazyToOneOption.PROXY)
    private CustomBranding customBranding;

    public List<Category> getCategories() {
        if (categories == null) {
            categories = new ArrayList<Category>();
        }
        return categories;
    }

    @SuppressWarnings("unused")
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Group> getGroups() {
        if (groups == null) {
            groups = new ArrayList<Group>();
        }
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public DomainType getDomainType() {
        return DomainType.ORGANIZATION;
    }

    public OrgStorageConfig getOrgStorageConfig() {
        return orgStorageConfigs.get(0);
    }

    public void setOrgStorageConfig(OrgStorageConfig orgStorageConfig) {
        if(!this.orgStorageConfigs.isEmpty()) {
            this.orgStorageConfigs.remove(0);
        }
        this.orgStorageConfigs.add(0, orgStorageConfig);
    }

    @Override
    public List<StorageConfiguration> getStorageConfigurations() {
        List<StorageConfiguration> storageConfigurations = new ArrayList<StorageConfiguration>();
        if (getOrgStorageConfig() != null && getOrgStorageConfig().getStorageConfigurations() != null) {
            storageConfigurations.addAll(getOrgStorageConfig().getStorageConfigurations());
        }

        return storageConfigurations;
    }

    public CustomBranding getCustomBranding() {
        return customBranding;
    }

    public void setCustomBranding(CustomBranding customBranding) {
        this.customBranding = customBranding;
    }

    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.ORGANIZATION;
    }
}
