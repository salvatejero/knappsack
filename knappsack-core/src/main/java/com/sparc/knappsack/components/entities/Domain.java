package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.Language;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A domain refers to a collection of users.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "DOMAIN")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class Domain extends BaseEntity {

    private static final long serialVersionUID = -2087765722987616566L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DOMAIN_TYPE", unique = false, nullable = false)
    @Enumerated(EnumType.STRING)
    private DomainType domainType;

    @Column(name = "NAME", nullable = false)
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "DOMAIN_CONFIGURATION_ID", nullable = false)
    @LazyToOne(LazyToOneOption.PROXY)
    private DomainConfiguration domainConfiguration;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "parentDomain", orphanRemoval = true)
    private List<KeyVaultEntry> keyVaultEntries;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
            name="KEY_VAULT_ENTRY_CHILD_DOMAIN",
            joinColumns={@JoinColumn(name="DOMAIN_ID", referencedColumnName="ID")},
            inverseJoinColumns={@JoinColumn(name="KEY_VAULT_ENTRY_ID", referencedColumnName="ID")})
    private List<KeyVaultEntry> childKeyVaultEntries;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "domain", orphanRemoval = true, targetEntity = Invitation.class)
    private List<Invitation> invitations;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "domain", orphanRemoval = true, targetEntity = UserDomain.class)
    private List<UserDomain> userDomains;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "DOMAIN_REGION", joinColumns = @JoinColumn(name = "DOMAIN_ID"), inverseJoinColumns = @JoinColumn(name = "REGION_ID"))
    private Set<Region> regions;

    @ElementCollection
    @Column(name = "LANGUAGE")
    @CollectionTable(name = "DOMAIN_LANGUAGE", joinColumns = @JoinColumn(name = "DOMAIN_ID"))
    @Enumerated(EnumType.STRING)
    private List<Language> languages;

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

    public DomainConfiguration getDomainConfiguration() {
        return initializeAndUnproxy(domainConfiguration);
    }

    public void setDomainConfiguration(DomainConfiguration domainConfiguration) {
        this.domainConfiguration = domainConfiguration;
    }

    public List<KeyVaultEntry> getKeyVaultEntries() {
        if (keyVaultEntries == null) {
            keyVaultEntries = new ArrayList<KeyVaultEntry>();
        }
        return keyVaultEntries;
    }

    public void setKeyVaultEntries(List<KeyVaultEntry> keyVaultEntries) {
        this.keyVaultEntries = keyVaultEntries;
    }

    public List<KeyVaultEntry> getChildKeyVaultEntries() {
        if (childKeyVaultEntries == null) {
            childKeyVaultEntries = new ArrayList<KeyVaultEntry>();
        }
        return childKeyVaultEntries;
    }

    public void setChildKeyVaultEntries(List<KeyVaultEntry> childKeyVaultEntries) {
        this.childKeyVaultEntries = childKeyVaultEntries;
    }

    public List<Invitation> getInvitations() {
        if (invitations == null) {
            invitations = new ArrayList<Invitation>();
        }
        return invitations;
    }

    public void setInvitations(List<Invitation> invitations) {
        this.invitations = invitations;
    }

    public List<UserDomain> getUserDomains() {
        if (userDomains == null) {
            userDomains = new ArrayList<UserDomain>();
        }
        return userDomains;
    }

    public void setUserDomains(List<UserDomain> userDomains) {
        this.userDomains = userDomains;
    }

    public Set<Region> getRegions() {
        if (regions == null) {
            regions = new HashSet<Region>();
        }
        return regions;
    }

    public void setRegions(Set<Region> regions) {
        this.regions = regions;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    @SuppressWarnings("all")
    @PrePersist
    private void prePersist() {
        if(domainConfiguration == null) {
            domainConfiguration = new DomainConfiguration();
        }

        if (domainType == null) {
            domainType = getDomainType();
        }
    }

    /**
     * @return DomainType - the specific domain type
     */
    public abstract DomainType getDomainType();

    /**
     * @return List - storage configurations for this domain
     */
    public abstract List<StorageConfiguration> getStorageConfigurations();

    public abstract OrgStorageConfig getOrgStorageConfig();
}
