package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.ApplicationVersionUserStatistic;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.models.ApplicationVersionStatisticSummaryModel;
import com.sparc.knappsack.models.ApplicationVersionUserStatisticModel;

import java.util.List;

public interface ApplicationVersionUserStatisticService extends EntityService<ApplicationVersionUserStatistic> {
    ApplicationVersionUserStatistic create(ApplicationVersion applicationVersion, User user, String remoteAddress, String userAgent);

    List<ApplicationVersionUserStatistic> get(ApplicationVersion applicationVersion, User user);

    List<ApplicationVersionUserStatistic> get(ApplicationVersion applicationVersion);

    List<ApplicationVersionUserStatistic> get(User user);

    List<ApplicationVersionUserStatistic> get(Application application);

    List<ApplicationVersionUserStatisticModel> toModels(List<ApplicationVersionUserStatistic> applicationVersionUserStatistics);

    ApplicationVersionUserStatisticModel toModel(ApplicationVersionUserStatistic applicationVersionUserStatistic);

    List<ApplicationVersionStatisticSummaryModel> getApplicationVersionUserStatisticSummaryModels(ApplicationVersion applicationVersion);

    List<ApplicationVersionStatisticSummaryModel> getApplicationVersionUserStatisticSummaryModels(Application application);

    long deleteAllForApplicationVersion(ApplicationVersion applicationVersion);
}
