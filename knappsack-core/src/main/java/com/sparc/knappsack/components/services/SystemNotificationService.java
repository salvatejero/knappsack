package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.SystemNotification;
import com.sparc.knappsack.enums.SystemNotificationType;
import com.sparc.knappsack.models.SystemNotificationModel;

import java.util.List;

public interface SystemNotificationService extends EntityService<SystemNotification> {

    public SystemNotification addSystemNotification(SystemNotificationModel systemNotificationModel);

    public SystemNotification editSystemNotification(SystemNotificationModel systemNotificationModel);

    public List<SystemNotificationModel> getAllModels(boolean filterByDate);

    public List<SystemNotification> getAll(boolean filterByDate);

    public List<SystemNotificationModel> getAllForTypes(boolean filterByDate, SystemNotificationType... types);
}
