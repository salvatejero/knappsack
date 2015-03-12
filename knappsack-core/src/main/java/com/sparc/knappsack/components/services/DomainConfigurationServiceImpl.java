package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.DomainConfigurationDao;
import com.sparc.knappsack.components.entities.DomainConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional( propagation = Propagation.REQUIRED )
@Service("domainConfigurationService")
public class DomainConfigurationServiceImpl implements DomainConfigurationService {

    @Autowired(required = true)
    private DomainConfigurationDao domainConfigurationDao;

    @Override
    public void add(DomainConfiguration domainConfiguration) {
        domainConfigurationDao.add(domainConfiguration);
    }

    @Override
    public DomainConfiguration get(Long id) {
        return domainConfigurationDao.get(id);
    }

    @Override
    public void delete(Long id) {
        domainConfigurationDao.delete(get(id));
    }

    @Override
    public void update(DomainConfiguration domainConfiguration) {
        domainConfigurationDao.update(domainConfiguration);
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
