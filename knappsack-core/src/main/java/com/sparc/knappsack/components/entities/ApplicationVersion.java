package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.EntityState;
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
 * An ApplicationVersion is the specific version of an application.  This entity refers to the specific installation file of the Application.
 */
@Entity
@Table(name = "APPLICATION_VERSION")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ApplicationVersion extends Storable implements Notifiable {

    private static final long serialVersionUID = 903961092354093124L;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID")
//    @Fetch(FetchMode.JOIN)
    private Application application;

    @Column(name = "VERSION_NAME")
    private String versionName;

    @Column(name = "APP_STATE")
    @Enumerated(EnumType.STRING)
    private AppState appState;

    @Column(name = "RECENT_CHANGES", length = 10000)
    private String recentChanges;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "INSTALLATION_FILE_ID")
    @JsonManagedReference
    @LazyToOne(value = LazyToOneOption.PROXY)
//    @Fetch(FetchMode.JOIN)
    private AppFile installationFile;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @JoinColumn(name = "PROVISIONING_PROFILE_ID")
    private AppFile provisioningProfile;

    @Column(name = "CF_BUNDLE_IDENTIFIER")
    private String cfBundleIdentifier;

    @Column(name = "CF_BUNDLE_VERSION")
    private String cfBundleVersion;

    @Column(name = "CF_BUNDLE_NAME")
    private String cfBundleName;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, targetEntity = Group.class)
    @JoinTable(name = "ORG_GROUP_GUEST_APPLICATION_VERSION", joinColumns = @JoinColumn(name = "APPLICATION_VERSION_ID"), inverseJoinColumns = @JoinColumn(name = "ORG_GROUP_ID"))
    @BatchSize(size=5)
    private List<Group> guestGroups;

    @Transient
    private EntityState state;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public AppState getAppState() {
        return appState;
    }

    public void setAppState(AppState appState) {
        this.appState = appState;
    }

    public String getRecentChanges() {
        return recentChanges;
    }

    public void setRecentChanges(String recentChanges) {
        this.recentChanges = recentChanges;
    }

    public AppFile getInstallationFile() {
        return installationFile;
    }

    public void setInstallationFile(AppFile installationFile) {
        this.installationFile = installationFile;
    }

    public AppFile getProvisioningProfile() {
        return provisioningProfile;
    }

    public void setProvisioningProfile(AppFile provisioningProfile) {
        this.provisioningProfile = provisioningProfile;
    }

    public String getCfBundleIdentifier() {
        return cfBundleIdentifier;
    }

    public void setCfBundleIdentifier(String cfBundleIdentifier) {
        this.cfBundleIdentifier = cfBundleIdentifier;
    }

    public String getCfBundleVersion() {
        return cfBundleVersion;
    }

    public void setCfBundleVersion(String cfBundleVersion) {
        this.cfBundleVersion = cfBundleVersion;
    }

    public String getCfBundleName() {
        return cfBundleName;
    }

    public void setCfBundleName(String cfBundleName) {
        this.cfBundleName = cfBundleName;
    }

    public StorableType getStorableType() {
        return StorableType.APPLICATION_VERSION;
    }

    public List<Group> getGuestGroups() {
        if (guestGroups == null) {
            guestGroups = new ArrayList<Group>();
        }
        return guestGroups;
    }

    public void setGuestGroups(List<Group> guestGroups) {
        this.guestGroups = guestGroups;
    }

    @Transient
    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.APPLICATION_VERSION;
    }

    @Transient
    public EntityState getState() {
        return this.state;
    }

    @PostLoad
    public void onPostLoad() {
        this.state = EntityState.LOADED;
    }

    @PostUpdate
    public void onPostUpdate() {
        this.state = EntityState.UPDATED;
    }

    @PostRemove
    public void onPostRemove() {
        this.state = EntityState.REMOVED;
    }

    @PostPersist
    public void onPostPersist() {
        this.state = EntityState.NEWLY_PERSISTED;
    }
}
