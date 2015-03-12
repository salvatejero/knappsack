package com.sparc.knappsack.components.entities;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = "REGION")
// @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Region extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinTable(name = "REGION_REGION_LOCALE", joinColumns = @JoinColumn(name = "REGION_ID"), inverseJoinColumns = @JoinColumn(name = "REGION_LOCALE_ID"))
    private Set<RegionLocale> regionLocales;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "REGION_EMAIL", joinColumns = @JoinColumn(name = "REGION_ID"))
    @Column(name = "EMAIL")
    private Set<String> emails;

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

    public Set<RegionLocale> getRegionLocales() {
        return regionLocales;
    }

    public void setRegionLocales(Set<RegionLocale> regionLocales) {
        this.regionLocales = regionLocales;
    }

    public Set<Locale> getLocales() {
        Set<Locale> locales = new HashSet<Locale>();
        for(RegionLocale regionLocale : getRegionLocales()) {
            locales.add(regionLocale.getLocale());
        }

        return locales;
    }

    public Set<String> getEmails() {
        if (emails == null) {
            emails = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        }
        return emails;
    }

    public void setEmails(Set<String> emails) {
        this.emails = emails;
    }
}
