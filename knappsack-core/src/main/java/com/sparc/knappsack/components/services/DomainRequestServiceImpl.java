package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.DomainRequestDao;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.DomainRequestForm;
import com.sparc.knappsack.models.DomainRequestModel;
import com.sparc.knappsack.models.DomainUserRequestModel;
import com.sparc.knappsack.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Transactional( propagation = Propagation.REQUIRED )
@Service("domainRequestService")
public class DomainRequestServiceImpl implements DomainRequestService {

    @Qualifier("domainRequestDao")
    @Autowired(required = true)
    private DomainRequestDao domainRequestDao;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("regionService")
    @Autowired(required = true)
    private RegionService regionService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("emailService")
    @Autowired(required = true)
    private EmailService emailService;

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Qualifier("roleService")
    @Autowired(required = true)
    private RoleService roleService;

    @Qualifier("domainEntityServiceFactory")
    @Autowired(required = true)
    private DomainEntityServiceFactory domainEntityServiceFactory;

    @Override
    public void add(DomainRequest domainRequest) {
        domainRequestDao.add(domainRequest);
    }

    @Override
    public DomainRequest get(Long id) {
        DomainRequest domainRequest = null;

        if (id != null && id > 0) {
            domainRequest = domainRequestDao.get(id);
        }

        return domainRequest;
    }

    @Override
    public void delete(Long id) {
        DomainRequest domainRequest = get(id);
        if(domainRequest != null) {
            domainRequestDao.delete(domainRequest);
        }
    }

    private void delete(DomainRequest domainRequest) {
        domainRequestDao.delete(domainRequest);
    }

    @Override
    public void update(DomainRequest domainRequest) {
        domainRequestDao.update(domainRequest);
    }

    @Override
    public List<DomainRequest> getAllForDomain(Long domainId) {
        List<DomainRequest> domainRequests = new ArrayList<DomainRequest>();

        if (domainId != null && domainId > 0) {
            List<DomainRequest> returnedDomainRequests = domainRequestDao.getAllForDomain(domainId);
            if (returnedDomainRequests != null) {
                domainRequests.addAll(returnedDomainRequests);
            }
        }

        return domainRequests;
    }

    @Override
    public DomainRequest createDomainRequest(DomainRequestForm domainRequestForm) {
        DomainRequest domainRequest = null;

        if (domainRequestForm != null && StringUtils.hasText(domainRequestForm.getFirstName()) && StringUtils.hasText(domainRequestForm.getLastName()) && StringUtils.hasText(domainRequestForm.getEmailAddress())) {

            Domain domain = domainService.getByUUID(domainRequestForm.getDomainUUID());
            if (domain != null) {

                Region region = regionService.get(domainRequestForm.getRegion());

                // If region exists and the specified domain contains the region is null
                if ((region != null && domain.getRegions().contains(region)) || (domainRequestForm.getRegion() != null && domainRequestForm.getRegion() > 0 && region != null) || (region == null)) {
                    domainRequest = new DomainRequest();

                    domainRequest.setFirstName(StringUtils.trimTrailingWhitespace(domainRequestForm.getFirstName()));
                    domainRequest.setLastName(StringUtils.trimTrailingWhitespace(domainRequestForm.getLastName()));

                    String emailAddress = StringUtils.trimTrailingWhitespace(domainRequestForm.getEmailAddress());
                    domainRequest.setEmailAddress(StringUtils.hasText(emailAddress) ? emailAddress.toLowerCase() : emailAddress);

                    domainRequest.setAddress(StringUtils.trimTrailingWhitespace(domainRequestForm.getAddress()));
                    domainRequest.setCompanyName(StringUtils.trimTrailingWhitespace(domainRequestForm.getCompanyName()));
                    domainRequest.setPhoneNumber(StringUtils.trimTrailingWhitespace(domainRequestForm.getPhoneNumber()));
                    domainRequest.setDeviceType(domainRequestForm.getDeviceType());
                    domainRequest.setLanguages(domainRequestForm.getLanguages());
                    domainRequest.setRegion(region);
                    domainRequest.setDomain(domain);
                    domainRequest.setStatus(Status.PENDING);

                    add(domainRequest);
                }

            }
        }

        return domainRequest;
    }

    @Override
    public boolean doesDomainRequestExist(Long domainId, String emailAddress) {
        boolean doesExist = false;
        if (domainId != null && domainId > 0 && StringUtils.hasText(emailAddress))  {
            doesExist = domainRequestDao.doesDomainRequestExist(domainId, StringUtils.trimTrailingWhitespace(emailAddress));
        }

        return doesExist;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

    @Override
    public long countAll(Long domainId) {
        return domainRequestDao.countAll(domainId);
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public DomainRequestModel toDomainRequestModel(DomainRequest domainRequest) {
        DomainRequestModel domainRequestModel = new DomainRequestModel();
        domainRequestModel.setAddress(domainRequest.getAddress());
        domainRequestModel.setCompanyName(domainRequest.getCompanyName());
        domainRequestModel.setDeviceType(domainRequest.getDeviceType());
        domainRequestModel.setDomainId(domainRequest.getDomain().getId());
        domainRequestModel.setEmailAddress(domainRequest.getEmailAddress());
        domainRequestModel.setId(domainRequest.getId());
        domainRequestModel.setFirstName(domainRequest.getFirstName());
        domainRequestModel.setLastName(domainRequest.getLastName());
        domainRequestModel.setPhoneNumber(domainRequest.getPhoneNumber());
        domainRequestModel.setLanguages(domainRequest.getLanguages());
        domainRequestModel.setRegion(regionService.createRegionModel(domainRequest.getRegion()));

        DomainEntityService domainEntityService = domainEntityServiceFactory.getDomainEntityService(domainRequest.getDomain().getDomainType());
        domainRequestModel.setAssignableDomains(domainEntityService.getAssignableDomainModelsForDomainRequest(domainRequest.getDomain()));

        return domainRequestModel;
    }

    @Override
    public List<DomainRequestModel> getAllDomainRequestModelsForDomain(Long domainId) {
        List<DomainRequestModel> domainRequestModels = new ArrayList<DomainRequestModel>();
        for (DomainRequest domainRequest : getAllForDomain(domainId)) {
            domainRequestModels.add(toDomainRequestModel(domainRequest));
        }

        return domainRequestModels;
    }
}
