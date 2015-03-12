package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.EventWatchDao;
import com.sparc.knappsack.components.entities.EventWatch;
import com.sparc.knappsack.components.entities.Notifiable;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.enums.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Transactional( propagation = Propagation.REQUIRED )
@Service("eventWatchService")
public class EventWatchServiceImpl implements EventWatchService {

    @Qualifier("eventWatchDao")
    @Autowired(required = true)
    private EventWatchDao eventWatchDao;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @Override
    public void add(EventWatch eventWatch) {
        eventWatchDao.add(eventWatch);
    }

    @Override
    public void delete(Long id) {
        eventWatchDao.delete(get(id));
    }

    @Override
    public void update(EventWatch eventWatch) {
        eventWatchDao.update(eventWatch);
    }

    private void save(EventWatch eventWatch) {
        if (eventWatch != null) {
            if (eventWatch.getId() != null && eventWatch.getId() > 0) {
                update(eventWatch);
            } else {
                add(eventWatch);
            }
        }
    }

    @Override
    public EventWatch get(Long id) {
        EventWatch bookmark = null;
        if (id != null && id > 0) {
            bookmark = eventWatchDao.get(id);
        }
        return bookmark;
    }

    @Override
    public EventWatch get(User user, Notifiable notifiable) {
        EventWatch eventWatch = null;

        if (user != null && notifiable != null) {
            eventWatch = eventWatchDao.get(user, notifiable.getId(), notifiable.getNotifiableType());
        }

        return eventWatch;
    }

    @Override
    public boolean createEventWatch(User user, Notifiable notifiable, EventType... eventTypes) {
        if (user != null && notifiable != null && eventTypes != null && eventTypes.length > 0) {
            EventWatch eventWatch = new EventWatch();
            eventWatch.setNotifiableId(notifiable.getId());
            eventWatch.setUser(user);
            eventWatch.setNotifiableType(notifiable.getNotifiableType());
            eventWatch.getEventTypes().addAll(Arrays.asList(eventTypes));

            save(eventWatch);

            return true;
        }

        return false;
    }

    @Override
    public boolean delete(User user, Notifiable notifiable) {
        EventWatch eventWatch = get(user, notifiable);

        if (eventWatch != null) {
            delete(eventWatch.getId());
            return true;
        }

        return false;
    }

    @Override
    public boolean doesEventWatchExist(User user, Notifiable notifiable) {
        EventWatch eventWatch = get(user, notifiable);

        return eventWatch != null;
    }

    @Override
    public boolean deleteAllEventWatchForNotifiable(Notifiable notifiable) {
        if (notifiable != null) {
            for (EventWatch eventWatch : getAll(notifiable)) {
                eventWatchDao.delete(eventWatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void sendNotifications(Notifiable notifiable, EventType... eventTypes) {
        for (EventType eventType : eventTypes) {
            EventDelivery eventDelivery = eventDeliveryFactory.getEventDelivery(eventType);
            eventDelivery.sendNotifications(notifiable);
        }
    }

    @Override
    public List<EventWatch> getAll(Notifiable notifiable) {
        List<EventWatch> eventWatches = new ArrayList<EventWatch>();
        if (notifiable != null) {
            eventWatches = eventWatchDao.getAll(notifiable);
        }

        return eventWatches;
    }

    @Override
    public List<EventWatch> getAll(Notifiable notifiable, EventType eventType) {
        List<EventWatch> eventWatches = new ArrayList<EventWatch>();
        if (notifiable != null && eventType != null) {
            eventWatches = eventWatchDao.getAll(notifiable.getId(), eventType);
        }

        return eventWatches;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
