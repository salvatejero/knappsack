package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.NotifiableType;
import com.sparc.knappsack.enums.StorableType;
import org.codehaus.jackson.annotate.JsonManagedReference;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * An Application is the generic portion of software hosted by Knappsack.
 */
@Entity
@Table(name = "APPLICATION")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Application extends Storable implements Notifiable {

    private static final long serialVersionUID = 1265568333226947048L;

    @Column(name = "NAME")
    private String name;

    @Column(name ="DESCRIPTION", length = 10000)
    private String description;

    @Column(name = "APPLICATION_TYPE")
    @Enumerated(EnumType.STRING)
    private ApplicationType applicationType;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = false)
    @JoinColumn(name = "ICON_ID")
    @LazyToOne(value = LazyToOneOption.NO_PROXY)
//    @Fetch(FetchMode.JOIN)
    private AppFile icon;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = false)
    @JoinTable(name = "APPLICATION_SCREENSHOT", joinColumns = @JoinColumn(name = "APPLICATION_ID"), inverseJoinColumns = @JoinColumn(name = "SCREENSHOT_ID"))
    @JsonManagedReference
    @BatchSize(size=30)
    private List<AppFile> screenshots = new ArrayList<AppFile>();

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "CATEGORY_ID")
    @Fetch(FetchMode.JOIN)
    private Category category;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID")
    @LazyToOne(value = LazyToOneOption.PROXY)
//    @Fetch(FetchMode.JOIN)
    private Group ownedGroup;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "application", orphanRemoval = true)
    @JsonManagedReference
    @BatchSize(size=5)
    private List<ApplicationVersion> applicationVersions = new ArrayList<ApplicationVersion>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

//    public AppFile getIcon() {
//        if (icons == null) {
//            icons = new ArrayList<AppFile>();
//        }
//        return (icons.size() > 0 ? icons.get(0) : null);
//    }
//
//    public void setIcon(AppFile icon) {
//        if (icons != null && !icons.isEmpty()) {
//            icons.remove(0);
//        } else if (icons == null) {
//            icons = new ArrayList<AppFile>();
//        }
//        this.icons.add(icon);
//    }

    public AppFile getIcon() {
        return icon;
    }

    public void setIcon(AppFile icon) {
        this.icon = icon;
    }

    public List<AppFile> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<AppFile> screenshots) {
        this.screenshots = screenshots;
    }

    public Category getCategory() {
        return initializeAndUnproxy(category);
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Group getOwnedGroup() {
        return ownedGroup;
    }

    public void setOwnedGroup(Group ownedGroup) {
        this.ownedGroup = ownedGroup;
    }

    public List<ApplicationVersion> getApplicationVersions() {
        return applicationVersions;
    }

    public void setApplicationVersions(List<ApplicationVersion> applicationVersions) {
        this.applicationVersions = applicationVersions;
    }

    public StorableType getStorableType() {
        return StorableType.APPLICATION;
    }

    @Transient
    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.APPLICATION;
    }
}
