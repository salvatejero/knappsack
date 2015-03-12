package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.StorageType;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * A StorageConfiguration is the basis for a description of where files are stored.
 */
@Entity
@Inheritance
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.STRING)
@Table(name = "STORAGE_CONFIGURATION")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class StorageConfiguration extends BaseEntity {

    private static final long serialVersionUID = -7395421937578331187L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", unique=true, nullable=false)
    private String name;

    @Column(name = "BASE_LOCATION")
    private String baseLocation;

    @Column(name = "STORAGE_TYPE")
    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    @Column(name = "REGISTRATION_DEFAULT")
    private boolean registrationDefault;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
         this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseLocation() {
        return baseLocation;
    }

    public void setBaseLocation(String baseLocation) {
        this.baseLocation = baseLocation;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public boolean isRegistrationDefault() {
        return registrationDefault;
    }

    public void setRegistrationDefault(boolean registrationDefault) {
        this.registrationDefault = registrationDefault;
    }
}
