package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.ApplicationType;
import org.codehaus.jackson.annotate.JsonManagedReference;

import javax.persistence.*;

@Entity
@Table(name = "IOS_KEY_VAULT_ENTRY")
public class IOSKeyVaultEntry extends KeyVaultEntry {

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "DISTRIBUTION_CERT_ID")
    @JsonManagedReference
    private AppFile distributionCert;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "DISTRIBUTION_KEY_ID")
    @JsonManagedReference
    private AppFile distributionKey;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "DISTRIBUTION_PROFILE_ID")
    @JsonManagedReference
    private AppFile distributionProfile;

    @Column(name = "DISTRIBUTION_KEY_PASSWORD", nullable = false)
    private String distributionKeyPassword;

    public AppFile getDistributionCert() {
        return distributionCert;
    }

    public void setDistributionCert(AppFile distributionCert) {
        this.distributionCert = distributionCert;
    }

    public AppFile getDistributionKey() {
        return distributionKey;
    }

    public void setDistributionKey(AppFile distributionKey) {
        this.distributionKey = distributionKey;
    }

    public AppFile getDistributionProfile() {
        return distributionProfile;
    }

    public void setDistributionProfile(AppFile distributionProfile) {
        this.distributionProfile = distributionProfile;
    }

    public String getDistributionKeyPassword() {
        return distributionKeyPassword;
    }

    @Transient
    public String getDistributionKeyPassword(boolean decrypt) {
        if (decrypt) {
            return decrypt(distributionKeyPassword);
        } else {
            return distributionKeyPassword;
        }
    }

    public void setDistributionKeyPassword(String distributionKeyPassword) {
        this.distributionKeyPassword = encrypt(distributionKeyPassword);
    }

    @Override
    public ApplicationType getApplicationType() {
        return ApplicationType.IOS;
    }
}
