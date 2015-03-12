package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.OrgStorageConfig;

import java.util.Date;

public interface RemoteStorageService extends StorageService {

    /**
     * @param appFile AppFile - the file to access via the URL
     * @param secondsToExpire - duration in seconds until the URL expires
     * @return String - the URL as a String
     */
    String getUrl(AppFile appFile, int secondsToExpire);

    /**
     * @param path String - path to append to URL
     * @return String - the build URL as a String
     */
    String buildPublicUrl(String path);

    /**
     * @param orgStorageConfig OrgStorageConfig - used to get the bandwidth tied to this Organization
     * @param start Date - get the bandwidth used beginning with this date
     * @param end Date - get the bandwidth used between the start date and this date
     * @return double - the bandwidth used for this Organization between the given start and end date;
     */
    double getMegabyteBandwidthUsed(OrgStorageConfig orgStorageConfig, Date start, Date end);
}
