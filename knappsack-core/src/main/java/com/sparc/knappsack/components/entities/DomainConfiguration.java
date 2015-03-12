package com.sparc.knappsack.components.entities;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(name = "DOMAIN_CONFIGURATION")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DomainConfiguration extends BaseEntity {

    private static final long serialVersionUID = -4164986380312034725L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DISABLED", nullable = false)
    private boolean disabledDomain;

    @Column(name = "DISABLE_LIMIT_VALIDATIONS", nullable = false)
    private boolean disableLimitValidations;

    @Column(name = "USER_LIMIT", nullable = false)
    private long userLimit;

    @Column(name = "APPLICATION_LIMIT", nullable = false)
    private long applicationLimit;

    @Column(name = "APP_VERSION_LIMIT", nullable = false)
    private long applicationVersionLimit;

    @Column(name = "MEGABYTE_STORAGE_LIMIT", nullable = false)
    private long megabyteStorageLimit;

    @Column(name = "MONITOR_BANDWIDTH", nullable = false)
    private boolean monitorBandwidth;

    @Column(name = "MEGABYTE_BANDWIDTH_LIMIT")
    private long megabyteBandwidthLimit;

    @Column(name = "BANDWIDTH_LIMIT_REACHED", nullable = false)
    private boolean bandwidthLimitReached;

    @Column(name = "APPLICATION_RESIGNER_ENABLED")
    private boolean applicationResignerEnabled;

    @Column(name = "CUSTOM_BRANDING_ENABLED")
    private boolean customBrandingEnabled;

    public DomainConfiguration() {
        applicationLimit = 2;
        applicationVersionLimit = 5;
        disabledDomain = false;
        disableLimitValidations = false;
        userLimit = 10;
        megabyteStorageLimit = 500;
        monitorBandwidth = false;
        megabyteBandwidthLimit = 2048;
        bandwidthLimitReached = false;
        applicationResignerEnabled = false;
        customBrandingEnabled = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isDisabledDomain() {
        return disabledDomain;
    }

    public void setDisabledDomain(boolean disabled) {
        this.disabledDomain = disabled;
    }

    public boolean isDisableLimitValidations() {
        return disableLimitValidations;
    }

    public void setDisableLimitValidations(boolean disableLimitValidations) {
        this.disableLimitValidations = disableLimitValidations;
    }

    public long getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(long userLimit) {
        this.userLimit = userLimit;
    }

    public long getApplicationLimit() {
        return applicationLimit;
    }

    public void setApplicationLimit(long applicationLimit) {
        this.applicationLimit = applicationLimit;
    }

    public long getApplicationVersionLimit() {
        return applicationVersionLimit;
    }

    public void setApplicationVersionLimit(long applicationVersionLimit) {
        this.applicationVersionLimit = applicationVersionLimit;
    }

    public long getMegabyteStorageLimit() {
        return megabyteStorageLimit;
    }

    public void setMegabyteStorageLimit(long megabyteStorageLimit) {
        this.megabyteStorageLimit = megabyteStorageLimit;
    }

    public boolean isMonitorBandwidth() {
        return monitorBandwidth;
    }

    public void setMonitorBandwidth(boolean monitorBandwidth) {
        this.monitorBandwidth = monitorBandwidth;
    }

    public long getMegabyteBandwidthLimit() {
        return megabyteBandwidthLimit;
    }

    public void setMegabyteBandwidthLimit(long megabyteBandwidthLimit) {
        this.megabyteBandwidthLimit = megabyteBandwidthLimit;
    }

    public boolean isBandwidthLimitReached() {
        return bandwidthLimitReached;
    }

    public void setBandwidthLimitReached(boolean bandwidthLimitReached) {
        this.bandwidthLimitReached = bandwidthLimitReached;
    }

    public boolean isApplicationResignerEnabled() {
        return applicationResignerEnabled;
    }

    public void setApplicationResignerEnabled(boolean applicationResignerEnabled) {
        this.applicationResignerEnabled = applicationResignerEnabled;
    }

    public boolean isCustomBrandingEnabled() {
        return customBrandingEnabled;
    }

    public void setCustomBrandingEnabled(boolean customBrandingEnabled) {
        this.customBrandingEnabled = customBrandingEnabled;
    }
}
