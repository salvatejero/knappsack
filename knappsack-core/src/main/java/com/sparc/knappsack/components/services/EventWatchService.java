package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.EventWatch;
import com.sparc.knappsack.components.entities.Notifiable;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.EventType;

import java.util.List;

public interface EventWatchService extends EntityService<EventWatch> {

    EventWatch get(User user, Notifiable notifiable);

    boolean createEventWatch(User user, Notifiable notifiable, EventType... eventTypes);

    boolean delete(User user, Notifiable notifiable);

    boolean doesEventWatchExist(User user, Notifiable notifiable);

    boolean deleteAllEventWatchForNotifiable(Notifiable notifiable);

    void sendNotifications(Notifiable notifiable, EventType... eventTypes);

    List<EventWatch> getAll(Notifiable notifiable);

    List<EventWatch> getAll(Notifiable notifiable, EventType eventType);

}
