package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.StorableType;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * A Storable entity is one that has one or many AppFile entities associated with it.  The purpose of this is to keep track of the files for
 * this Storable entity.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "STORABLE")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class Storable extends BaseEntity {

    private static final long serialVersionUID = 131232227317295130L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "STORAGE_CONFIGURATION_ID")
    private StorageConfiguration storageConfiguration;

    @Column(name = "STORABLE_TYPE", unique = false, nullable = false)
    @Enumerated(EnumType.STRING)
    private StorableType storableType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StorageConfiguration getStorageConfiguration() {
        return initializeAndUnproxy(storageConfiguration);
    }

    public void setStorageConfiguration(StorageConfiguration storageConfiguration) {
        this.storageConfiguration = storageConfiguration;
    }

    public abstract StorableType getStorableType();

    @SuppressWarnings("all")
    @PrePersist
    protected void prePersist() {
        if (storableType == null) {
            storableType = getStorableType();
        }
    }
}
