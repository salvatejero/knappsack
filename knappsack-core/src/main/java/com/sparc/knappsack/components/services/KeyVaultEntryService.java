package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.KeyVaultEntry;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.forms.KeyVaultEntryForm;
import com.sparc.knappsack.models.KeyVaultEntryModel;

import java.util.List;

public interface KeyVaultEntryService extends EntityService<KeyVaultEntry> {

    List<KeyVaultEntry> getAllForDomain(Long domainId);

    KeyVaultEntry createKeyVaultEntry(KeyVaultEntryForm form);

    KeyVaultEntry editKeyVaultEntry(KeyVaultEntryForm form);

    KeyVaultEntryModel convertToModel(KeyVaultEntry keyVaultEntry);

    List<KeyVaultEntry> getAllForUser(User user);

    List<KeyVaultEntry> getAllForDomain(Domain domain);

    List<KeyVaultEntry> getAllForDomainAndApplicationType(Domain domain, ApplicationType applicationType);

    List<KeyVaultEntry> getAllForDomainAndApplicationType(Long domainId, ApplicationType applicationType);

    void deleteAllForDomain(Domain domain);
}
