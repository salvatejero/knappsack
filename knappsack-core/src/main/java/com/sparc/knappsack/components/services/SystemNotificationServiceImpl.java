package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.SystemNotificationDao;
import com.sparc.knappsack.components.entities.SystemNotification;
import com.sparc.knappsack.enums.SystemNotificationType;
import com.sparc.knappsack.models.SystemNotificationModel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional( propagation = Propagation.REQUIRED )
@Service("systemNotificationService")
public class SystemNotificationServiceImpl implements SystemNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SystemNotificationServiceImpl.class);

    @Qualifier("systemNotificationDao")
    @Autowired(required = true)
    private SystemNotificationDao systemNotificationDao;

    private void save(SystemNotification systemNotification) {
        if (systemNotification != null) {
            if (systemNotification.getId() == null || systemNotification.getId() <= 0) {
                systemNotificationDao.add(systemNotification);
            } else {
                systemNotificationDao.update(systemNotification);
            }
        }
    }

    @Override
    public void add(SystemNotification systemNotification) {
        save(systemNotification);
    }

    @Override
    public SystemNotification get(Long id) {
        SystemNotification systemNotification = null;
        if (id != null && id > 0) {
            systemNotification = systemNotificationDao.get(id);
        }
        return systemNotification;
    }

    @Override
    public List<SystemNotification> getAll(boolean filterByDate) {
        List<SystemNotification> systemNotifications = systemNotificationDao.getAll();

        if (filterByDate) {
            Date currentDate = new Date();
            List<SystemNotification> filteredList = new ArrayList<SystemNotification>();
            for (SystemNotification systemNotification : systemNotifications) {
                if (isDateInDateRange(currentDate, systemNotification.getStartDate(), systemNotification.getEndDate())) {
                    filteredList.add(systemNotification);
                }
            }
            return filteredList;
        } else {
            return systemNotifications;
        }
    }

    @Override
    public void delete(Long id) {
        SystemNotification systemNotification = get(id);
        if (systemNotification != null) {
            delete(systemNotification);
        }
    }

    private void delete(SystemNotification systemNotification) {
        if (systemNotification != null) {
            systemNotificationDao.delete(systemNotification);
        }
    }

    @Override
    public void update(SystemNotification systemNotification) {
        save(systemNotification);
    }

    @Override
    public SystemNotification addSystemNotification(SystemNotificationModel systemNotificationModel) {
        if (systemNotificationModel != null) {
            SystemNotification systemNotification = new SystemNotification();
            systemNotification.setStartDate(systemNotificationModel.getStartDate());
            systemNotification.setEndDate(systemNotificationModel.getEndDate());
            systemNotification.setMessage(StringUtils.trim(systemNotificationModel.getMessage()));
            systemNotification.setAllPages(systemNotificationModel.isAllPages());
            systemNotification.setNotificationType(systemNotificationModel.getNotificationType());
            systemNotification.setNotificationSeverity(systemNotificationModel.getNotificationSeverity());

            save(systemNotification);

            return systemNotification;
        }

        return null;
    }

    @Override
    public SystemNotification editSystemNotification(SystemNotificationModel systemNotificationModel) {
        if (systemNotificationModel != null) {
            SystemNotification systemNotification = get(systemNotificationModel.getId());
            if (systemNotification != null) {
                systemNotification.setStartDate(systemNotificationModel.getStartDate());
                systemNotification.setEndDate(systemNotificationModel.getEndDate());
                systemNotification.setMessage(StringUtils.trim(systemNotificationModel.getMessage()));
                systemNotification.setAllPages(systemNotificationModel.isAllPages());
                systemNotification.setNotificationType(systemNotificationModel.getNotificationType());
                systemNotification.setNotificationSeverity(systemNotificationModel.getNotificationSeverity());

                save(systemNotification);

                return systemNotification;
            }
        }

        return null;
    }

    @Override
    public List<SystemNotificationModel> getAllModels(boolean filterByDate) {
        List<SystemNotificationModel> models = new ArrayList<SystemNotificationModel>();

        List<SystemNotification> systemNotifications = getAll(filterByDate);
        if (systemNotifications != null) {
            for (SystemNotification systemNotification : systemNotifications) {
                SystemNotificationModel model = createSystemNotificationModel(systemNotification);

                if (model != null) {
                    models.add(model);
                }
            }
        }

        return models;
    }

    @Override
    public List<SystemNotificationModel> getAllForTypes(boolean filterByDate, SystemNotificationType... types) {
        List<SystemNotificationModel> models = new ArrayList<SystemNotificationModel>();
        Date currentDate = new Date();

        if (types != null) {
            for (SystemNotification systemNotification : systemNotificationDao.getAllForTypes(types)) {
                if (filterByDate) {
                    Date startDate = systemNotification.getStartDate();
                    Date endDate = systemNotification.getEndDate();
                    if (isDateInDateRange(currentDate, startDate, endDate)) {
                        models.add(createSystemNotificationModel(systemNotification));
                    }
                } else {
                    models.add(createSystemNotificationModel(systemNotification));
                }
            }
        }

        for (SystemNotification systemNotification : systemNotificationDao.getAllForAllPages()) {
            if (filterByDate) {
                Date startDate = systemNotification.getStartDate();
                Date endDate = systemNotification.getEndDate();
                if (isDateInDateRange(currentDate, startDate, endDate)) {
                    SystemNotificationModel model = createSystemNotificationModel(systemNotification);
                    if (!models.contains(model)) {
                        models.add(model);
                    }
                }
            } else {
                SystemNotificationModel model = createSystemNotificationModel(systemNotification);
                if (!models.contains(model)) {
                    models.add(model);
                }
            }
        }

        return models;
    }

    private boolean isDateInDateRange(Date dateToCheck, Date startDate, Date endDate) {
        boolean isInRange = false;

        if (dateToCheck != null) {

            if ((startDate == null || (startDate != null && startDate.before(dateToCheck))) && (endDate == null || (endDate != null && endDate.after(dateToCheck)))) {
                isInRange = true;
            }

        }

        return isInRange;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

    private SystemNotificationModel createSystemNotificationModel(SystemNotification systemNotification) {
        SystemNotificationModel model = null;
        if (systemNotification != null) {
            model = new SystemNotificationModel();

            model.setId(systemNotification.getId());
            model.setStartDate(systemNotification.getStartDate());
            model.setEndDate(systemNotification.getEndDate());
            model.setMessage(systemNotification.getMessage());
            model.setNotificationType(systemNotification.getNotificationType());
            model.setNotificationSeverity(systemNotification.getNotificationSeverity());
            model.setAllPages(systemNotification.isAllPages());
        }
        return model;
    }
}
