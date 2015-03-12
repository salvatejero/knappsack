package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.DomainUserRequestDao;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.DomainUserRequest;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.entities.UserDomain;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service("domainUserRequestService")
public class DomainUserRequestServiceImpl implements DomainUserRequestService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("domainUserRequestDao")
    @Autowired(required = true)
    private DomainUserRequestDao domainUserRequestDao;

    @Qualifier("userDomainService")
    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @Override
    public DomainUserRequest get(Long id) {
        return domainUserRequestDao.get(id);
    }

    @Override
    public void delete(Long requestId) {
        if (requestId != null && requestId > 0) {
            DomainUserRequest request = get(requestId);

            if (request != null) {
                domainUserRequestDao.delete(request);
            }
        }
    }

    @Override
    public void update(DomainUserRequest domainUserRequest) {
        domainUserRequestDao.update(domainUserRequest);
    }

    @Override
    public void add(DomainUserRequest domainUserRequest) {
        domainUserRequestDao.add(domainUserRequest);
    }

    @Override
    public DomainUserRequest createDomainUserRequest(User user, String accessCode) {
        DomainUserRequest domainUserRequest = null;
        if (user != null && accessCode != null && !accessCode.isEmpty()) {

            Domain domain = domainService.getByUUID(accessCode);

            if (domain != null) {

                //Check if user is already in group or not
                if (userDomainService.get(user, domain.getId()) == null) {
                    //Check if there is already a pendingRequest for this given user and this group
                    if (!doesRequestExist(user, domain, Status.PENDING)) {
                        domainUserRequest = new DomainUserRequest();
                        domainUserRequest.setDomain(domain);
                        domainUserRequest.setUser(user);
                        domainUserRequest.setStatus(Status.PENDING);

                        add(domainUserRequest);
                    }

                }
            }
        }

        return domainUserRequest;
    }

    @Override
    public boolean doesRequestExist(User user, Domain domain, Status status) {
        if (user != null && domain != null && status != null) {
            List<DomainUserRequest> requests = getAll(domain.getId());
            if (requests != null && requests.size() > 0) {
                for (DomainUserRequest request : requests) {
                    if (user.getId().equals(request.getUser().getId()) && status.equals(request.getStatus())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<DomainUserRequest> getAll(Long domainId) {
        return getAll(domainService.get(domainId));
    }

    @Override
    public List<DomainUserRequest> getAll(Domain domain) {
        List<DomainUserRequest> domainUserRequests = new ArrayList<DomainUserRequest>();

        if (domain != null) {
            domainUserRequests.addAll(domainUserRequestDao.getAllRequests(domain));
        }

        return domainUserRequests;
    }

    @Override
    public List<DomainUserRequest> getAll(Long domainId, Status status) {
        return getAll(domainService.get(domainId), status);
    }

    @Override
    public List<DomainUserRequest> getAll(Domain domain, Status status) {
        List<DomainUserRequest> domainUserRequests = new ArrayList<DomainUserRequest>();

        if (domain != null && status != null) {
            domainUserRequests.addAll(domainUserRequestDao.getAllRequests(domain, status));
        }

        return domainUserRequests;
    }

    @Override
    public boolean acceptRequest(DomainUserRequest domainUserRequest, UserRole userRole) {
        boolean success = false;
        if (domainUserRequest != null && userRole != null) {
            UserDomain userDomain = userService.addUserToDomain(domainUserRequest.getUser(), domainUserRequest.getDomain(), userRole);

            if (userDomain != null) {
                domainUserRequest.setStatus(Status.ACCEPTED);
//                update(domainUserRequest);

                EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.DOMAIN_USER_ACCESS_REQUEST_CONFIRMATION);
                if (deliveryMechanism != null) {
                    if (!deliveryMechanism.sendNotifications(domainUserRequest)) {
                        log.error("Error sending Group Access Request Confirmation Email:", domainUserRequest);
                    }
                }

                delete(domainUserRequest.getId());
            }
        }

        return success;
    }

    @Override
    public boolean declineRequest(DomainUserRequest domainUserRequest) {
        boolean success = false;
        if (domainUserRequest != null) {

            domainUserRequest.setStatus(Status.DECLINED);
//            update(domainUserRequest);

            EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.DOMAIN_USER_ACCESS_REQUEST_CONFIRMATION);
            if (deliveryMechanism != null) {
                success = deliveryMechanism.sendNotifications(domainUserRequest);
                if (!success) {
                    log.error("Error sending Domain User Access Request Confirmation Email:", domainUserRequest);
                }
            }

            delete(domainUserRequest.getId());

        }

        return success;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
