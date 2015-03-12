package com.sparc.knappsack.components.entities;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An OrgStorageConfig associates and Organization to a collection of StorageConfig entities.
 *
 * NOTE: The model supports multiple StorageConfig entities for a single Organization; However, this is not currently supported by
 * the business logic and front end.
 */
@Entity
@Table(name = "ORG_STORAGE_CONFIG")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class OrgStorageConfig extends BaseEntity {

    private static final long serialVersionUID = 7160416296577678618L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PREFIX", unique = true, nullable = false)
    private String prefix;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "ORG_STORAGE_CONFIG_STORAGE_CONFIGURATION", joinColumns = @JoinColumn(name = "ORG_STORAGE_CONFIG_ID"), inverseJoinColumns = @JoinColumn(name = "STORAGE_CONFIGURATION_ID"))
    private List<StorageConfiguration> storageConfigurations = new ArrayList<StorageConfiguration>();

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "ORGANIZATION_ID")
    @Fetch(FetchMode.JOIN)
    private Organization organization;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<StorageConfiguration> getStorageConfigurations() {
        if (storageConfigurations == null) {
            storageConfigurations = new ArrayList<StorageConfiguration>();
        }
        return storageConfigurations;
    }

    @SuppressWarnings("unused")
    public void setStorageConfigurations(List<StorageConfiguration> storageConfigurations) {
        this.storageConfigurations = storageConfigurations;
    }

    public Organization getOrganization() {
        return organization;
    }

    @SuppressWarnings("unused")
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
