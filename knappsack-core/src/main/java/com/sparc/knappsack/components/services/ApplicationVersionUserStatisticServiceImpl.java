package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.ApplicationVersionUserStatisticDao;
import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.ApplicationVersionUserStatistic;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.models.ApplicationVersionStatisticSummaryModel;
import com.sparc.knappsack.models.ApplicationVersionUserStatisticModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Transactional
@Service("applicationVersionUserStatisticService")
public class ApplicationVersionUserStatisticServiceImpl implements ApplicationVersionUserStatisticService {

    @Autowired(required = true)
    @Qualifier("applicationVersionUserStatisticDao")
    private ApplicationVersionUserStatisticDao applicationVersionUserStatisticDao;

    @Override
    public void add(ApplicationVersionUserStatistic applicationVersionUserStatistic) {
        applicationVersionUserStatisticDao.add(applicationVersionUserStatistic);
    }

    @Override
    public ApplicationVersionUserStatistic get(Long id) {
        return applicationVersionUserStatisticDao.get(id);
    }

    @Override
    public void delete(Long id) {
        applicationVersionUserStatisticDao.delete(get(id));
    }

    @Override
    public void update(ApplicationVersionUserStatistic applicationVersionUserStatistic) {
        applicationVersionUserStatisticDao.update(applicationVersionUserStatistic);
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

    public List<ApplicationVersionUserStatistic> get(ApplicationVersion applicationVersion, User user) {
        return applicationVersionUserStatisticDao.get(applicationVersion, user);
    }

    public List<ApplicationVersionUserStatistic> get(ApplicationVersion applicationVersion) {
        return applicationVersionUserStatisticDao.get(applicationVersion);
    }

    public List<ApplicationVersionUserStatistic> get(User user) {
        return applicationVersionUserStatisticDao.get(user);
    }

    @Override
    public List<ApplicationVersionUserStatistic> get(Application application) {
        return applicationVersionUserStatisticDao.get(application);
    }

    public List<ApplicationVersionStatisticSummaryModel> getApplicationVersionUserStatisticSummaryModels(ApplicationVersion applicationVersion) {
        return toApplicationVersionUserStatisticSummaryModel(get(applicationVersion));
    }

    public List<ApplicationVersionStatisticSummaryModel> getApplicationVersionUserStatisticSummaryModels(Application application) {
        return toApplicationVersionUserStatisticSummaryModel(get(application));
    }

    @Override
    public long deleteAllForApplicationVersion(ApplicationVersion applicationVersion) {
        long numRowsDeleted = 0;
        if (applicationVersion != null) {
            numRowsDeleted = applicationVersionUserStatisticDao.deleteAllForApplicationVersion(applicationVersion);
        }

        return numRowsDeleted;
    }

    private List<ApplicationVersionStatisticSummaryModel> toApplicationVersionUserStatisticSummaryModel(List<ApplicationVersionUserStatistic> applicationVersionUserStatistics) {
        ApplicationVersionStatisticSummaryModel applicationVersionUserStatisticSummaryModel = new ApplicationVersionStatisticSummaryModel();
        List<ApplicationVersionStatisticSummaryModel> applicationVersionUserStatisticSummaryModelList = new CopyOnWriteArrayList<ApplicationVersionStatisticSummaryModel>();
        if (applicationVersionUserStatistics == null || applicationVersionUserStatistics.isEmpty()) {
            return applicationVersionUserStatisticSummaryModelList;
        }

        applicationVersionUserStatisticSummaryModel.setApplicationName(applicationVersionUserStatistics.get(0).getApplicationVersion().getApplication().getName());
        applicationVersionUserStatisticSummaryModel.setApplicationVersionId(applicationVersionUserStatistics.get(0).getApplicationVersion().getId());
        applicationVersionUserStatisticSummaryModel.setApplicationVersionName(applicationVersionUserStatistics.get(0).getApplicationVersion().getVersionName());
        applicationVersionUserStatisticSummaryModelList.add(applicationVersionUserStatisticSummaryModel);

        for (ApplicationVersionUserStatistic applicationVersionUserStatistic : applicationVersionUserStatistics) {
            boolean listHasStatistic = false;
            for (ApplicationVersionStatisticSummaryModel versionUserStatisticSummaryModel : applicationVersionUserStatisticSummaryModelList) {
                if (versionUserStatisticSummaryModel.getApplicationVersionId().equals(applicationVersionUserStatistic.getApplicationVersion().getId())) {
                    versionUserStatisticSummaryModel.setTotal(versionUserStatisticSummaryModel.getTotal() + 1);
                    listHasStatistic = true;
                    break;
                }
            }

            if (!listHasStatistic) {
                ApplicationVersionStatisticSummaryModel summaryModel = new ApplicationVersionStatisticSummaryModel();
                summaryModel.setTotal(1);
                summaryModel.setApplicationName(applicationVersionUserStatistic.getApplicationVersion().getApplication().getName());
                summaryModel.setApplicationVersionId(applicationVersionUserStatistic.getApplicationVersion().getId());
                summaryModel.setApplicationVersionName(applicationVersionUserStatistic.getApplicationVersion().getVersionName());
                applicationVersionUserStatisticSummaryModelList.add(summaryModel);
            }
        }

        return applicationVersionUserStatisticSummaryModelList;
    }

    public List<ApplicationVersionUserStatisticModel> toModels(List<ApplicationVersionUserStatistic> applicationVersionUserStatistics) {
        List<ApplicationVersionUserStatisticModel> applicationVersionUserStatisticModels = new ArrayList<ApplicationVersionUserStatisticModel>();
        for (ApplicationVersionUserStatistic applicationVersionUserStatistic : applicationVersionUserStatistics) {
            applicationVersionUserStatisticModels.add(toModel(applicationVersionUserStatistic));
        }

        return applicationVersionUserStatisticModels;
    }

    public ApplicationVersionUserStatisticModel toModel(ApplicationVersionUserStatistic applicationVersionUserStatistic) {
        ApplicationVersionUserStatisticModel applicationVersionUserStatisticModel = new ApplicationVersionUserStatisticModel();
        applicationVersionUserStatisticModel.setApplicationVersionId(applicationVersionUserStatistic.getApplicationVersion().getId());
        applicationVersionUserStatisticModel.setId(applicationVersionUserStatistic.getId());
        applicationVersionUserStatisticModel.setRemoteAddress(applicationVersionUserStatistic.getRemoteAddress());
        applicationVersionUserStatisticModel.setUserId(applicationVersionUserStatistic.getUser().getId());
        applicationVersionUserStatisticModel.setUserAgent(applicationVersionUserStatistic.getUserAgent());

        applicationVersionUserStatisticModel.setUserName(applicationVersionUserStatistic.getUser().getFullName());
        applicationVersionUserStatisticModel.setUserEmail(applicationVersionUserStatistic.getUser().getEmail());
        applicationVersionUserStatisticModel.setApplicationVersionName(applicationVersionUserStatistic.getApplicationVersion().getVersionName());
        applicationVersionUserStatisticModel.setApplicationName(applicationVersionUserStatistic.getApplicationVersion().getApplication().getName());
        applicationVersionUserStatisticModel.setDate(applicationVersionUserStatistic.getCreateDate());

        return applicationVersionUserStatisticModel;
    }

    @Override
    public ApplicationVersionUserStatistic create(ApplicationVersion applicationVersion, User user, String remoteAddress, String userAgent) {
        ApplicationVersionUserStatistic applicationVersionUserStatistic = new ApplicationVersionUserStatistic();
        applicationVersionUserStatistic.setApplicationVersion(applicationVersion);
        applicationVersionUserStatistic.setUser(user);
        applicationVersionUserStatistic.setRemoteAddress(remoteAddress);
        applicationVersionUserStatistic.setUserAgent(userAgent);
        add(applicationVersionUserStatistic);

        return applicationVersionUserStatistic;
    }
}
