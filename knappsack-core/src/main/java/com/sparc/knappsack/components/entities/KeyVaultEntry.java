package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.StorableType;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import javax.persistence.*;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "KEY_VAULT_ENTRY")
public abstract class KeyVaultEntry extends Storable implements KeyVaultEntryInterface {

    @Transient
    private static final String HASH_KEY = "O2$Bp4^J4n";

    @Transient
    private StandardPBEStringEncryptor encryptor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PARENT_DOMAIN_ID", nullable = false)
    private Domain parentDomain;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
            name="KEY_VAULT_ENTRY_CHILD_DOMAIN",
            joinColumns={@JoinColumn(name="KEY_VAULT_ENTRY_ID", referencedColumnName="ID")},
            inverseJoinColumns={@JoinColumn(name="DOMAIN_ID", referencedColumnName="ID")})
    private List<Domain> childDomains;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "APPLICATION_TYPE", unique = false, nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationType applicationType;

    public List<Domain> getChildDomains() {
        return childDomains;
    }

    public void setChildDomains(List<Domain> childDomains) {
        this.childDomains = childDomains;
    }

    public Domain getParentDomain() {
        return parentDomain;
    }

    public void setParentDomain(Domain parentDomain) {
        this.parentDomain = parentDomain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract ApplicationType getApplicationType();

    public StorableType getStorableType() {
        return StorableType.KEY_VAULT_ENTRY;
    }

    protected String encrypt(String message) {
        return getEncryptor().encrypt(message);
    }

    protected String decrypt(String message) {
        return getEncryptor().decrypt(message);
    }

    private StandardPBEStringEncryptor getEncryptor() {
        if (encryptor == null) {
            encryptor = new StandardPBEStringEncryptor();
            encryptor.setProvider(new BouncyCastleProvider());
            encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
            encryptor.setPassword(this.getUuid() + HASH_KEY);
        }
        return encryptor;
    }

    @SuppressWarnings("all")
    @PrePersist
    protected void prePersist() {
        super.prePersist();
        if (applicationType == null) {
            applicationType = getApplicationType();
        }
    }
}
