package com.sparc.knappsack.components.entities;

import javax.persistence.*;
import java.util.Locale;

@Entity
@Table(name = "REGION_LOCALE")
// @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RegionLocale extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "COUNTRY", nullable = false)
    private String country;

    @Column(name = "LANGUAGE", nullable = false)
    private String language;

    @Column(name = "VARIANT", nullable = true)
    private String variant;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public Locale getLocale() {
        if(variant != null) {
            return new Locale(language, country, variant);
        }

        return new Locale(language, country);
    }
}
