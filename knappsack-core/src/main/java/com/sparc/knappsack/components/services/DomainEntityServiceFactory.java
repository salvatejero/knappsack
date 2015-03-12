package com.sparc.knappsack.components.services;

import com.sparc.knappsack.enums.DomainType;

public interface DomainEntityServiceFactory {
    DomainEntityService getDomainEntityService(DomainType domainType);
}
