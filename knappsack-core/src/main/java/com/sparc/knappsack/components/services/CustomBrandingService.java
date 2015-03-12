package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.CustomBranding;

public interface CustomBrandingService extends EntityService<CustomBranding> {

    <D> D getCustomBrandingModel(CustomBranding customBranding, Class<D> modelClass);

    CustomBranding getBySubdomain(String subdomain);
}
