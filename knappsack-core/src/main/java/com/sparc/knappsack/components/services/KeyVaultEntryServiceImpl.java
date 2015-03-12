package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.KeyVaultEntryDao;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.KeyVaultEntry;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.forms.KeyVaultEntryForm;
import com.sparc.knappsack.models.KeyVaultEntryModel;
import com.sparc.knappsack.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Transactional
@Service("keyVaultEntryService")
public class KeyVaultEntryServiceImpl implements KeyVaultEntryService {
    private static final Logger log = LoggerFactory.getLogger(KeyVaultEntryServiceImpl.class);

    @Qualifier("keyVaultEntryDao")
    @Autowired(required = true)
    private KeyVaultEntryDao keyVaultEntryDao;

    @Qualifier("keyVaultServiceFactory")
    @Autowired(required = true)
    private KeyVaultServiceFactory keyVaultServiceFactory;

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("domainEntityServiceFactory")
    @Autowired(required = true)
    private DomainEntityServiceFactory domainEntityServiceFactory;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    private  void save(KeyVaultEntry keyVaultEntry) {
        if (keyVaultEntry != null) {
            if (keyVaultEntry.getId() == null || keyVaultEntry.getId() <= 0) {
                keyVaultEntryDao.add(keyVaultEntry);
            } else {
                keyVaultEntryDao.update(keyVaultEntry);
            }
        }
    }

    @Override
    public void add(KeyVaultEntry keyVaultEntry) {
        if (keyVaultEntry != null) {
            keyVaultEntryDao.add(keyVaultEntry);
        }
    }

    @Override
    public KeyVaultEntry get(Long id) {
        KeyVaultEntry keyVaultEntry = null;
        if (id != null && id > 0) {
            keyVaultEntry = keyVaultEntryDao.get(id);
        }
        return keyVaultEntry;
    }

    @Override
    public List<KeyVaultEntry> getAllForDomain(Long domainId) {
        return getAllForDomain(domainService.get(domainId));
    }

    @Override
    public void delete(Long id) {
        KeyVaultEntry keyVaultEntry = get(id);
        if (keyVaultEntry != null) {
            deleteFiles(keyVaultEntry);
            keyVaultEntryDao.delete(keyVaultEntry);
        }
    }

    private void deleteFiles(KeyVaultEntry keyVaultEntry) {
        if (keyVaultEntry != null) {
            KeyVaultService keyVaultService = keyVaultServiceFactory.getKeyVaultService(keyVaultEntry.getApplicationType());
            if (keyVaultService != null) {
                keyVaultService.deleteFiles(keyVaultEntry);
            }
        }
    }

    @Override
    public void update(KeyVaultEntry keyVaultEntry) {
        save(keyVaultEntry);
    }

    @Override
    public KeyVaultEntry createKeyVaultEntry(KeyVaultEntryForm keyVaultEntryForm) {
        KeyVaultEntry keyVaultEntry = null;

        if (keyVaultEntryForm != null) {
            KeyVaultService keyVaultService = keyVaultServiceFactory.getKeyVaultService(keyVaultEntryForm.getApplicationType());

            if (keyVaultService != null) {
                keyVaultEntry = keyVaultService.createKeyVaultEntry(keyVaultEntryForm);
            }

        }

        return keyVaultEntry;
    }

    @Override
    public KeyVaultEntry editKeyVaultEntry(KeyVaultEntryForm keyVaultEntryForm) {
        KeyVaultEntry keyVaultEntry = get(keyVaultEntryForm.getId());
        if (keyVaultEntry != null) {
            List<Long> childDomainIds = keyVaultEntryForm.getChildDomainIds();
            List<Domain> childDomains =  domainService.get(childDomainIds.toArray(new Long[childDomainIds.size()]));

            keyVaultEntry.setChildDomains(childDomains);

            keyVaultEntry.setName(StringUtils.trimTrailingWhitespace(keyVaultEntryForm.getName()));

            update(keyVaultEntry);
        }
        return keyVaultEntry;
    }

    @Override
    public KeyVaultEntryModel convertToModel(KeyVaultEntry keyVaultEntry) {
        KeyVaultEntryModel model = null;
        if (keyVaultEntry != null) {
            model = new KeyVaultEntryModel();
            model.setId(keyVaultEntry.getId());
            model.setName(keyVaultEntry.getName());

            Domain parentDomain = keyVaultEntry.getParentDomain();
            if (parentDomain != null) {
                DomainEntityService domainEntityService = domainEntityServiceFactory.getDomainEntityService(parentDomain.getDomainType());
                if (domainEntityService != null) {
                    model.setParentDomain(domainEntityService.createDomainModel(parentDomain));
                }
            }

            for (Domain childDomain : keyVaultEntry.getChildDomains()) {
                DomainEntityService domainEntityService = domainEntityServiceFactory.getDomainEntityService(childDomain.getDomainType());
                if (domainEntityService != null) {
                    model.getChildDomains().add(domainEntityService.createDomainModel(childDomain));
                }
            }

            model.setApplicationType(keyVaultEntry.getApplicationType());
            model.setCreateDate(keyVaultEntry.getCreateDate());

            User user = userService.getByEmail(keyVaultEntry.getChangedBy());
            if (user != null) {
                UserModel userModel = new UserModel();
                userModel.setFirstName(user.getFirstName());
                userModel.setLastName(user.getLastName());
                userModel.setEmail(user.getEmail());

                model.setCreatedBy(userModel);
            }
        }
        return model;
    }

    @Override
    public List<KeyVaultEntry> getAllForUser(User user) {
        List<KeyVaultEntry> keyVaultEntries = new ArrayList<KeyVaultEntry>();
        if (user != null) {
            Organization activeOrg = user.getActiveOrganization();
            if (activeOrg != null) {
                keyVaultEntries.addAll(activeOrg.getKeyVaultEntries());
            }
        }
        return keyVaultEntries;
    }

    @Override
    public List<KeyVaultEntry> getAllForDomain(Domain domain) {
        List<KeyVaultEntry> entries = new ArrayList<KeyVaultEntry>();
        if (domain != null) {
            Set<KeyVaultEntry> keyVaultEntries = new HashSet<KeyVaultEntry>();
            keyVaultEntries.addAll(domain.getKeyVaultEntries());
            keyVaultEntries.addAll(domain.getChildKeyVaultEntries());

            entries.addAll(keyVaultEntries);
        }
        return entries;
    }

    @Override
    public List<KeyVaultEntry> getAllForDomainAndApplicationType(Domain domain, ApplicationType applicationType) {
        List<KeyVaultEntry> entries = new ArrayList<KeyVaultEntry>();
        if (domain != null && applicationType != null) {

            DomainEntityService domainEntityService = domainEntityServiceFactory.getDomainEntityService(domain.getDomainType());
            if (domainEntityService != null && domainEntityService.isApplicationResignerEnabled(domain)) {
                for (KeyVaultEntry entry : getAllForDomain(domain)) {
                    if (ApplicationType.getAllInGroup(entry.getApplicationType()).contains(applicationType)) {
                        entries.add(entry);
                    }
                }
            }
        }
        return entries;
    }

    @Override
    public List<KeyVaultEntry> getAllForDomainAndApplicationType(Long domainId, ApplicationType applicationType) {
        return getAllForDomainAndApplicationType(domainService.get(domainId), applicationType);
    }

    @Override
    public void deleteAllForDomain(Domain domain) {
        if (domain != null) {
            boolean entriesExist = false;
            for (KeyVaultEntry keyVaultEntry : domain.getKeyVaultEntries()) {
                entriesExist = true;
                deleteFiles(keyVaultEntry);

                for (Domain childDomain : keyVaultEntry.getChildDomains()) {
                    childDomain.getChildKeyVaultEntries().remove(keyVaultEntry);
                }
            }
            if (entriesExist) {
                keyVaultEntryDao.deleteAllForDomain(domain);
            }
        }
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
