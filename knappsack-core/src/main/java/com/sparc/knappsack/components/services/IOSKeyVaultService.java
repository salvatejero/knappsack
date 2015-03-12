package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.IOSKeyVaultEntry;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.forms.KeyVaultEntryForm;
import com.sparc.knappsack.models.SQSResignerModel;
import com.sparc.knappsack.util.WebRequest;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

@Transactional( propagation = Propagation.REQUIRED )
@Service("iosKeyVaultService")
public class IOSKeyVaultService extends AbstractKeyVaultService<IOSKeyVaultEntry> {

    private static final Logger log = LoggerFactory.getLogger(IOSKeyVaultService.class);

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("keyVaultEntryService")
    @Autowired(required = true)
    private KeyVaultEntryService keyVaultEntryService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Override
    public IOSKeyVaultEntry createKeyVaultEntry(KeyVaultEntryForm keyVaultEntryForm) {
        IOSKeyVaultEntry keyVaultEntry = null;
        User user = userService.getUserFromSecurityContext();

        if (keyVaultEntryForm != null && user != null && user.getActiveOrganization() != null) {
            Domain parentDomain = user.getActiveOrganization();
            if (parentDomain != null) {
                keyVaultEntry = new IOSKeyVaultEntry();
                keyVaultEntry.setName(StringUtils.trimTrailingWhitespace(keyVaultEntryForm.getName()));
                keyVaultEntry.setDistributionKeyPassword(keyVaultEntryForm.getDistributionKeyPassword());
                keyVaultEntry.setStorageConfiguration(getStorageConfiguration(parentDomain));
                keyVaultEntry.setParentDomain(parentDomain);

                List<Long> childDomainIds = keyVaultEntryForm.getChildDomainIds();
                List<Domain> childDomains =  domainService.get(childDomainIds.toArray(new Long[childDomainIds.size()]));
                keyVaultEntry.setChildDomains(childDomains);

                parentDomain.getKeyVaultEntries().add(keyVaultEntry);

                keyVaultEntryService.add(keyVaultEntry);

                // Must add AppFiles after KeyVaultEntry is already added in order to comply with AppFile.Storable constraint.
                keyVaultEntry.setDistributionCert(saveFile(keyVaultEntryForm.getDistributionCert(), parentDomain, keyVaultEntry));
                keyVaultEntry.setDistributionKey(saveFile(keyVaultEntryForm.getDistributionKey(), parentDomain, keyVaultEntry));
                keyVaultEntry.setDistributionProfile(saveFile(keyVaultEntryForm.getDistributionProfile(), parentDomain, keyVaultEntry));
            }
        }

        return keyVaultEntry;
    }

    @Override
    public void deleteFiles(IOSKeyVaultEntry keyVaultEntry) {
        if (keyVaultEntry != null) {
            deleteAppFile(keyVaultEntry.getDistributionCert());
            deleteAppFile(keyVaultEntry.getDistributionKey());
            deleteAppFile(keyVaultEntry.getDistributionProfile());

            keyVaultEntry.setDistributionCert(null);
            keyVaultEntry.setDistributionKey(null);
            keyVaultEntry.setDistributionProfile(null);

//            Domain parentDomain = keyVaultEntry.getParentDomain();
//            if (parentDomain != null) {
//                parentDomain.getKeyVaultEntries().remove(keyVaultEntry);
//            }
//
//            List<Domain> childDomains = keyVaultEntry.getChildDomains();
//            for (Domain domain : childDomains) {
//                domain.getChildKeyVaultEntries().remove(keyVaultEntry);
//            }
//
//            keyVaultEntryDao.delete(keyVaultEntry);
        }
    }

    @Override
    public boolean resign(IOSKeyVaultEntry keyVaultEntry, ApplicationVersion applicationVersion, AppState requestedAppState) {
        boolean success = false;
        WebRequest webRequest = WebRequest.getInstance();
        if (keyVaultEntry != null && keyVaultEntry instanceof IOSKeyVaultEntry && applicationVersion != null && requestedAppState != null && webRequest != null) {
            SQSResignerModel model = new SQSResignerModel();

            model.setBucket(getBucketName(applicationVersion));
            model.setApplicationType(keyVaultEntry.getApplicationType());
            model.setFileToSign(applicationVersion.getInstallationFile().getRelativePath());
            model.setDistributionCert(keyVaultEntry.getDistributionCert().getRelativePath());
            model.setDistributionKey((keyVaultEntry).getDistributionKey().getRelativePath());
            model.setDistributionKeyPassword((keyVaultEntry).getDistributionKeyPassword());
            model.setDistributionProfile((keyVaultEntry).getDistributionProfile().getRelativePath());

            NameValuePair requestedAppStateParam = new BasicNameValuePair("appState", requestedAppState.name());
            NameValuePair initiationUserParam = new BasicNameValuePair("user", applicationVersion.getChangedBy());
            model.setCallbackUrl(webRequest.generateURL(String.format("/resigner/webhook/%s", applicationVersion.getId()), requestedAppStateParam, initiationUserParam));

            if (checkAllRequiredFields(model)) {
                success = super.resign(model);
            }
        }
        return success;
    }

    private boolean checkAllRequiredFields(SQSResignerModel model) {
        boolean isValid = true;
        if (model != null) {

            if (!StringUtils.hasText(model.getBucket())) {
                isValid = false;
            }

            if (isValid && model.getApplicationType() == null) {
                isValid = false;
            }

            if (isValid && !StringUtils.hasText(model.getFileToSign())) {
                isValid = false;
            }

            if (isValid && !StringUtils.hasText(model.getDistributionCert())) {
                isValid = false;
            }

            if (isValid && !StringUtils.hasText(model.getDistributionKey())) {
                isValid = false;
            }

            if (isValid && !StringUtils.hasText(model.getDistributionKeyPassword())) {
                isValid = false;
            }

            if (isValid && !StringUtils.hasText(model.getDistributionProfile())) {
                isValid = false;
            }

            if (isValid && !StringUtils.hasText(model.getCallbackUrl())) {
                isValid = false;
            }

        }
        return isValid;
    }

    public static void main(String... args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            System.out.println(String.format("Formatted Date: %s", sdf.parse("2013-06-14T01:03:00Z")));
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
