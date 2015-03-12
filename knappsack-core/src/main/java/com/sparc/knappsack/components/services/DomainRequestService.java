package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.DomainRequest;
import com.sparc.knappsack.forms.DomainRequestForm;
import com.sparc.knappsack.models.DomainRequestModel;

import java.util.List;

public interface DomainRequestService extends EntityService<DomainRequest> {

    /**
     * @param domainId Id of the domain to use for lookup
     * @return List of all requests for the given domain.
     */
    List<DomainRequest> getAllForDomain(Long domainId);

    DomainRequest createDomainRequest(DomainRequestForm domainRequestForm);

    boolean doesDomainRequestExist(Long domainId, String emailAddress);

    long countAll(Long domainId);

    DomainRequestModel toDomainRequestModel(DomainRequest domainRequest);

    List<DomainRequestModel>  getAllDomainRequestModelsForDomain(Long domainId);

}
