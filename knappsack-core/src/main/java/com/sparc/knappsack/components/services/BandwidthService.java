package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Organization;

public interface BandwidthService {

    /**
     * @param applicationVersion ApplicationVersion - check to see if the Organization for this ApplicationVersion has reached the bandwidth limit
     * @return boolean true if the bandwidth limit for the Organization has already been reached
     */
    boolean isBandwidthLimitReached(ApplicationVersion applicationVersion);

    /**
     * @param application Application - check to see if the Organization for this Application has reached the bandwidth limit
     * @return boolean true if the bandwidth limit for the Organization has already been reached
     */
    boolean isBandwidthLimitReached(Application application);

    /**
     * @param organizationId Long - look up the bandwidth used for the organization with the given ID
     * @return double - the total amount of bandwidth used since the creation of the organization.
     */
    double getMegabyteBandwidthUsed(Long organizationId);

    double getMegabyteBandwidthUsed(Organization organization);
}
