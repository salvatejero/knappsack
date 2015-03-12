package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.StorableType;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.*;

/**
 * A category is a division of applications with a specific purpose.
 */
@Entity
@Table(name = "CATEGORY")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Category extends Storable {

    private static final long serialVersionUID = -6859847730817266053L;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ICON_ID")
    @LazyToOne(value = LazyToOneOption.NO_PROXY)
//    @Fetch(FetchMode.JOIN)
    private AppFile icon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

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

    public AppFile getIcon() {
        return icon;
    }

    public void setIcon(AppFile icon) {
        this.icon = icon;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public StorableType getStorableType() {
        return StorableType.CATEGORY;
    }
}
