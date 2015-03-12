package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Region;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.DomainType;

import java.util.List;

public interface DomainService {

    /**
     * @param domainId Long - the ID of the Domain to lookup
     * @return Domain - the domain that matches the ID
     */
    Domain get(Long domainId);

    /**
     * @param domainIds - collection of IDs for domain Entities
     * @return List of domains with the given ids
     */
    List<Domain> get(Long... domainIds);

    Domain getByUUID(String domainUUID);

    /**
     * @param domain Domain - The domain to check.
     * @return Whether or not the application resigner is enabled for the given domain.
     */
    boolean isApplicationResignerEnabled(Domain domain);

    /**
     * @param regionId Id of the region to use for lookup
     * @return The domain which owns the given region
     */
    Domain getDomainForRegion(Long regionId);

    /**
     * @param region Region to use for lookup
     * @return The domain which owns the given region
     */
    Domain getDomainForRegion(Region region);

    /**
     * @param domain Domain to search on
     * @param includeParentDomainsIfEmpty Whether or not to include parent domain admins if none exist for specified domain
     * @return
     */
    List<User> getAllAdmins(Domain domain, boolean includeParentDomainsIfEmpty);

    /**
     * @param domainId Id of the domain to search on
     * @param regionName Name of the Region to search for
     * @return
     */
    boolean doesDomainContainRegionWithName(Long domainId, String regionName);

    /**
     * @param user User - get domains for this user
     * @param domainTypes - the domain must be one of these types
     * @return List of domains that this user belongs to with the given types
     */
    List<Domain> getAll(User user, DomainType... domainTypes);
}
