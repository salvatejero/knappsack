package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.RegionDao;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Region;
import com.sparc.knappsack.forms.DomainRegionForm;
import com.sparc.knappsack.models.RegionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Transactional( propagation = Propagation.REQUIRED )
@Service("regionService")
public class RegionServiceImpl implements RegionService {

    @Qualifier("regionDao")
    @Autowired(required = true)
    private RegionDao regionDao;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    private void save(Region region) {
        if (region != null) {
            if (region.getId() == null || region.getId() <= 0) {
                regionDao.add(region);
            } else {
                regionDao.update(region);
            }
        }
    }

    @Override
    public void add(Region region) {
        regionDao.add(region);
    }

    @Override
    public Region get(Long id) {
        Region region = null;

        if (id != null && id > 0) {
            region = regionDao.get(id);
        }

        return region;
    }

    @Override
    public void delete(Long id) {
        Region region = get(id);

        if (region != null) {
            Domain domain = domainService.getDomainForRegion(region);

            if (domain != null) {
                domain.getRegions().remove(region);
            }
            regionDao.delete(region);
        }
    }

    @Override
    public void update(Region region) {
        regionDao.update(region);
    }

    @Override
    public Region createRegion(DomainRegionForm regionForm) {
        Region region = null;

        if (regionForm != null) {
            Domain domain = domainService.get(regionForm.getDomainId());

            if (domain != null) {
                region = new Region();
                region.setName(regionForm.getName());
                region.getEmails().addAll(regionForm.getEmails());

                domain.getRegions().add(region);

                add(region);
            }
        }

        return region;
    }

    @Override
    public Region editRegion(DomainRegionForm regionForm) {
        Region region = null;

        if (regionForm != null) {
            region = get(regionForm.getId());

            if (region != null) {
                region.setName(regionForm.getName());
                region.getEmails().clear();
                region.getEmails().addAll(regionForm.getEmails());

                update(region);
            }
        }

        return region;
    }

    @Override
    public RegionModel createRegionModel(Region region) {
        RegionModel model = null;

        if (region != null) {
            model = new RegionModel();

            model.setId(region.getId());
            model.setName(region.getName());
            List<String> emails = new ArrayList<String>(region.getEmails());
            Collections.sort(emails, String.CASE_INSENSITIVE_ORDER);
            model.getEmails().addAll(emails);
        }

        return model;
    }

    @Override
    public List<Region> getAllForDomain(Long domainId) {
        List<Region> regions = new ArrayList<Region>();
        Domain domain = domainService.get(domainId);

        if (domain != null) {
            regions.addAll(domain.getRegions());
        }

        return regions;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
