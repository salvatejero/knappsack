package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.OrgStorageConfig;

public interface OrgStorageConfigService extends EntityService<OrgStorageConfig>{

    OrgStorageConfig getByPrefix(String prefix);
}
