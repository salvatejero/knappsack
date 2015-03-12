package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.OrgStorageConfigDao;
import com.sparc.knappsack.components.entities.OrgStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional( propagation = Propagation.REQUIRED )
@Service("orgStorageConfigService")
public class OrgStorageConfigServiceImpl implements OrgStorageConfigService {

    @Qualifier("orgStorageConfigDao")
    @Autowired(required = true)
    private OrgStorageConfigDao orgStorageConfigDao;

    @Override
    public OrgStorageConfig get(Long id) {
        return orgStorageConfigDao.get(id);
    }

    @Override
    public void delete(Long id) {
        orgStorageConfigDao.delete(get(id));
    }

    @Override
    public void update(OrgStorageConfig orgStorageConfig) {
        orgStorageConfigDao.update(orgStorageConfig);
    }

    @Override
    public void add(OrgStorageConfig orgStorageConfig) {
        orgStorageConfigDao.add(orgStorageConfig);
    }

    @Override
    public OrgStorageConfig getByPrefix(String prefix) {
        return orgStorageConfigDao.get(prefix);
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

}
