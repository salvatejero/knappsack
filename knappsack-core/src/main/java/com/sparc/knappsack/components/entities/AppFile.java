package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.StorageType;
import org.codehaus.jackson.annotate.JsonBackReference;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * An AppFile is an entity representation of an actual file on a file system.  An AppFile can be an image, installation file, document, etc...
 */
@Entity
@Table(name = "APP_FILE")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AppFile extends BaseEntity {

    private static final long serialVersionUID = -922503072652756916L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "RELATIVE_PATH")
    private String relativePath;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "SIZE", nullable = true)
    private Double size = 0.0;

    @Column(name = "STORAGE_TYPE")
    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "STORABLE_ID", nullable = false)
    @JsonBackReference
    private Storable storable;

    public Long getId() {
        return id;
    }

    /**
     * @return String - the name of this AppFile.  Typically, this will be the file name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name String - the name of this AppFile.  Typically, this will be the name of the actual file.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return String - the path relative to the base location of the storage configuration.  The General pattern for this is
     * the organziation prefix followed by the storable UUID, file type and name.
     */
    public String getRelativePath() {
        return relativePath;
    }

    /**
     * @param relativePath String - the path relative to the base location of the storage configuration.  The General pattern for this is
     * the organziation prefix followed by the storable UUID, file type and name.
     */
    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    /**
     *
     * @return String - the content type of the file
     */
    public String getType() {
        return type;
    }

    /**
     * @param type String - the content type of the file
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return double - the size of the file in megabytes
     */
    public Double getSize() {
        return size == null ? 0.0 : size;
    }

    /**
     * @param size - the size of the file in megabytes
     */
    public void setSize(Double size) {
        this.size = size;
    }

    /**
     * @return StorageType - how the file is actually stored
     */
    public StorageType getStorageType() {
        return storageType;
    }

    /**
     * @param storageType StorageType - how the file is actually stored
     */
    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    /**
     * @return Storable - the parent entity to which this AppFile belongs.  For example, an Application entity has fields for an icon and screenshots.
     * Those icon and screenshots are represented by AppFile entities.  In this case the Application is the Storable entity.
     */
    public Storable getStorable() {
        return storable;
    }

    /**
     * @param storable Storable - the parent entity to which this AppFile belongs.  For example, an Application entity has fields for an icon and screenshots.
     * Those icon and screenshots are represented by AppFile entities.  In this case the Application is the Storable entity.
     */
    public void setStorable(Storable storable) {
        this.storable = storable;
    }

    /**
     * @return String - returns the base location of the storage configuration plus the relative path of this AppFile.
     */
    public String getAbsolutePath() {
        return getStorable().getStorageConfiguration().getBaseLocation() + getRelativePath();
    }
}
