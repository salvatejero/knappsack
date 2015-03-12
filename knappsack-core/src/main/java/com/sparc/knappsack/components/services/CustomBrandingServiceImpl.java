package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.CustomBrandingDao;
import com.sparc.knappsack.components.entities.CustomBranding;
import com.sparc.knappsack.components.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("customBrandingService")
public class CustomBrandingServiceImpl implements CustomBrandingService {

    @Autowired(required = true)
    private CustomBrandingDao customBrandingDao;

    @Qualifier("mapper")
    @Autowired(required = true)
    private Mapper mapper;

    @Override
    public <D> D getCustomBrandingModel(CustomBranding customBranding, Class<D> modelClass) {
        return mapper.map(customBranding, modelClass);
    }

    @Override
    public void add(CustomBranding customBranding) {
        customBrandingDao.add(customBranding);
    }

    @Override
    public CustomBranding get(Long id) {
        return customBrandingDao.get(id);
    }

    @Override
    public void delete(Long id) {
        customBrandingDao.delete(get(id));
    }

    @Override
    public void update(CustomBranding customBranding) {
        customBrandingDao.update(customBranding);
    }

    public CustomBranding getBySubdomain(String subdomain) {
        return customBrandingDao.getBySubdomain(subdomain);
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
