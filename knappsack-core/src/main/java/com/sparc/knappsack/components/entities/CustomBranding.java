package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.StorableType;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.*;

@Entity
@Table(name = "CUSTOM_BRANDING")
public class CustomBranding extends Storable {

    private static final long serialVersionUID = 6777669014287786315L;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "LOGO_ID")
    @LazyToOne(value = LazyToOneOption.NO_PROXY)
    private AppFile logo;

    @Column(name = "EMAIL_HEADER", nullable = true)
    private String emailHeader;

    @Column(name = "EMAIL_FOOTER", nullable = true)
    private String emailFooter;

    @Column(name = "SUBDOMAIN", unique = true, nullable = true)
    private String subdomain;

    public AppFile getLogo() {
        return logo;
    }

    public void setLogo(AppFile logo) {
        this.logo = logo;
    }

    public String getEmailHeader() {
        return emailHeader;
    }

    public void setEmailHeader(String emailHeader) {
        this.emailHeader = emailHeader;
    }

    public String getEmailFooter() {
        return emailFooter;
    }

    public void setEmailFooter(String emailFooter) {
        this.emailFooter = emailFooter;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    @Override
    public StorableType getStorableType() {
        return StorableType.CUSTOM_BRANDING;
    }
}
