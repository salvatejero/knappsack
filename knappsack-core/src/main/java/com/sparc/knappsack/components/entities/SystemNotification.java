package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.SystemNotificationSeverity;
import com.sparc.knappsack.enums.SystemNotificationType;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "SYSTEM_NOTIFICATION")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SystemNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "START_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(name = "END_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Column(name = "MESSAGE", nullable = false, length = 1000)
    private String message;

    @Column(name = "ALL_PAGES", nullable = false)
    private boolean allPages;

    @Column(name = "NOTIFICATION_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private SystemNotificationType notificationType;

    @Column(name = "NOTIFICATION_SEVERITY", nullable = false)
    @Enumerated(EnumType.STRING)
    private SystemNotificationSeverity notificationSeverity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAllPages() {
        return allPages;
    }

    public void setAllPages(boolean allPages) {
        this.allPages = allPages;
    }

    public SystemNotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(SystemNotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public SystemNotificationSeverity getNotificationSeverity() {
        return notificationSeverity;
    }

    public void setNotificationSeverity(SystemNotificationSeverity notificationSeverity) {
        this.notificationSeverity = notificationSeverity;
    }
}
