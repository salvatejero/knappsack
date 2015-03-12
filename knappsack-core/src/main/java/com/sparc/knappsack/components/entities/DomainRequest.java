package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.DeviceType;
import com.sparc.knappsack.enums.Language;
import com.sparc.knappsack.enums.NotifiableType;
import com.sparc.knappsack.enums.Status;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = "DOMAIN_REQUEST", uniqueConstraints = {@UniqueConstraint(columnNames={"DOMAIN_ID", "EMAIL_ADDRESS"})})
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DomainRequest extends BaseEntity implements Notifiable {

    private static final long serialVersionUID = 1831410842043658067L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "COMPANY_NAME")
    private String companyName;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "EMAIL_ADDRESS", nullable = false)
    private String emailAddress;

    @Column(name = "DEVICE_TYPE")
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "LANGUAGE")
    @CollectionTable(name = "DOMAIN_REQUEST_LANGUAGE", joinColumns = @JoinColumn(name = "DOMAIN_REQUEST_ID"))
    @Enumerated(EnumType.STRING)
    private Set<Language> languages;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "DOMAIN_ID", nullable = false)
    private Domain domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REGION_ID")
    private Region region;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.DOMAIN_REQUEST;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public Set<Language> getLanguages() {
        if (languages == null) {
            languages = new TreeSet<Language>();
        }
        return languages;
    }

    public void setLanguages(Set<Language> languages) {
        this.languages = languages;
    }

    public Domain getDomain() {
        return initializeAndUnproxy(domain);
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
