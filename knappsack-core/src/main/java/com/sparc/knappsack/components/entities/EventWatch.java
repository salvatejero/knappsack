package com.sparc.knappsack.components.entities;


import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.NotifiableType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "EVENT_WATCH",
        uniqueConstraints = {@UniqueConstraint(columnNames={"NOTIFIABLE_TYPE", "USER_ID", "NOTIFIABLE_ID"})}
)
// @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EventWatch extends BaseEntity {

    private static final long serialVersionUID = -7046760432772496845L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "NOTIFIABLE_ID")
    private Long notifiableId;

    @Column(name = "NOTIFIABLE_TYPE")
    @Enumerated(EnumType.STRING)
    private NotifiableType notifiableType;

    @ElementCollection(targetClass = EventType.class)
    @CollectionTable(name = "EVENT_WATCH_TYPE",
            joinColumns = @JoinColumn(name = "EVENT_WATCH_ID"))
    @Column(name = "EVENT_TYPE_ID")
    private List<EventType> eventTypes = new ArrayList<EventType>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @SuppressWarnings("unused")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @SuppressWarnings("unused")
    public Long getNotifiableId() {
        return notifiableId;
    }

    public void setNotifiableId(Long notifiableId) {
        this.notifiableId = notifiableId;
    }

    @SuppressWarnings("unused")
    public NotifiableType getNotifiableType() {
        return notifiableType;
    }

    public void setNotifiableType(NotifiableType notifiableType) {
        this.notifiableType = notifiableType;
    }

    public List<EventType> getEventTypes() {
        return eventTypes;
    }

    @SuppressWarnings("unused")
    public void setEventTypes(List<EventType> eventTypes) {
        this.eventTypes = eventTypes;
    }
}
