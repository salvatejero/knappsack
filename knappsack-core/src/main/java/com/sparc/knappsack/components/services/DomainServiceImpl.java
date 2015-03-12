package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.DomainDao;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Region;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.DomainType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service("domainService")
public class DomainServiceImpl implements DomainService {

    @Qualifier("domainDao")
    @Autowired(required = true)
    private DomainDao domainDao;

    @Qualifier("domainEntityServiceFactory")
    @Autowired(required = true)
    private DomainEntityServiceFactory domainEntityServiceFactory;

    @Override
    public Domain get(Long domainId) {
        Domain domain = null;
        if (domainId != null && domainId > 0) {
            domain = domainDao.get(domainId);
        }
        return domain;
    }

    @Override
    public List<Domain> get(Long... domainIds) {
        List<Domain> domains = new ArrayList<Domain>();
        if (domainIds != null && domainIds.length > 0) {
            List<Domain> returnedDomains = domainDao.get(domainIds);
            if (returnedDomains != null) {
                domains.addAll(returnedDomains);
            }
        }
        return domains;
    }

    @Override
    public Domain getByUUID(String uuid) {
        Domain domain = null;

        if (StringUtils.hasText(uuid)) {
            domain = domainDao.getByUUID(uuid);
        }

        return domain;
    }

    @Override
    public boolean isApplicationResignerEnabled(Domain domain) {
        boolean isEnabled = false;

        if (domain != null) {
            DomainEntityService domainEntityService = domainEntityServiceFactory.getDomainEntityService(domain.getDomainType());
            if (domainEntityService != null) {
                isEnabled = domainEntityService.isApplicationResignerEnabled(domain);
            }
        }

        return isEnabled;
    }

    @Override
    public Domain getDomainForRegion(Long regionId) {
        Domain domain = null;

        if (regionId != null && regionId > 0) {
            domain = domainDao.getByRegion(regionId);
        }

        return domain;
    }

    @Override
    public Domain getDomainForRegion(Region region) {
        Domain domain = null;

        if (region != null) {
            domain = domainDao.getByRegion(region);
        }

        return domain;
    }

    @Override
    public List<User> getAllAdmins(Domain domain, boolean includeParentDomainsIfEmpty) {
        List<User> admins = new ArrayList<User>();
        if (domain != null) {
            DomainEntityService domainEntityService = domainEntityServiceFactory.getDomainEntityService(domain.getDomainType());
            if (domainEntityService != null) {
                admins.addAll(domainEntityService.getAllAdmins(domain, includeParentDomainsIfEmpty));
            }
        }

        return admins;
    }

    @Override
    public boolean doesDomainContainRegionWithName(Long domainId, String regionName) {
        boolean exists = false;

        if (StringUtils.hasText(regionName) && domainId != null && domainId > 0) {
            exists = domainDao.doesDomainContainRegionWithName(domainId, regionName.trim());
        }

        return exists;
    }

    @Override
    public List<Domain> getAll(User user, DomainType... domainTypes) {
        return domainDao.getAll(user, domainTypes);
    }
}
