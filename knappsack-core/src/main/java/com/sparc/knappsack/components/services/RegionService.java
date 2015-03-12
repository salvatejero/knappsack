package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Region;
import com.sparc.knappsack.forms.DomainRegionForm;
import com.sparc.knappsack.models.RegionModel;

import java.util.List;

public interface RegionService extends EntityService<Region> {

    Region createRegion(DomainRegionForm regionForm);

    Region editRegion(DomainRegionForm regionForm);

    RegionModel createRegionModel(Region region);

    List<Region> getAllForDomain(Long domainId);
}
