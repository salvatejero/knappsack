package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.RegionLocaleDao;
import com.sparc.knappsack.components.entities.RegionLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional( propagation = Propagation.REQUIRED )
@Service("regionLocaleService")
public class RegionLocaleServiceImpl implements RegionLocaleService {

    @Autowired(required = true)
    private RegionLocaleDao regionLocaleDao;

    @Override
    public void add(RegionLocale regionLocale) {
        regionLocaleDao.add((regionLocale));
    }

    @Override
    public RegionLocale get(Long id) {
        return regionLocaleDao.get(id);
    }

    @Override
    public void delete(Long id) {
        regionLocaleDao.delete(get(id));
    }

    @Override
    public void update(RegionLocale regionLocale) {
        regionLocaleDao.update(regionLocale);
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
