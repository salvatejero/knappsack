package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.forms.ApplicationVersionForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ApplicationVersionValidator implements Validator {
    private static final double MEGABYTE_CONVERSION = 1048576;

    private static final String VERSION_NAME_FIELD = "versionName";
    private static final String RECENT_CHANGES_FIELD = "recentChanges";
    private static final String APP_STATE_FIELD = "appState";
    private static final String APP_FILE_FIELD = "appFile";
    private static final String KEY_VAULT_ENTRY_FIELD = "keyVaultEntryId";

    @Qualifier("applicationVersionService")
    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("keyVaultEntryService")
    @Autowired(required = true)
    private KeyVaultEntryService keyVaultEntryService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return ApplicationVersionForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ApplicationVersionForm version = (ApplicationVersionForm) target;

        Application application = applicationService.get(version.getParentId());
        User user = userService.getUserFromSecurityContext();

        validateOrganizationLimits(errors, version, application, user.getActiveOrganization());
        validateVersionName(errors, version);
        validateRecentChanges(errors, version);
        validateAppState(errors, version);
        validateInstallFile(errors, version, application.getApplicationType());

        validateResign(version, application.getOwnedGroup(), application.getApplicationType(), errors);
    }

    protected void validateOrganizationLimits(Errors errors, ApplicationVersionForm version, Application parentApplication, Organization organization) {
        if(!version.isEditing()) {
            if (parentApplication != null) {
                validateApplicationVersionLimit(parentApplication, organization, errors);
            }
            validateOrganizationStorageLimit(organization, version.getAppFile().getSize(), errors);
        }
    }

    protected void validateInstallFile(Errors errors, ApplicationVersionForm version, ApplicationType parentApplicationType) {
        if (version.isEditing()) {
            if (version.getAppFile() != null && !version.getAppFile().isEmpty()) {
                validateExtension(version, parentApplicationType, errors);
            }
        } else {
            if (version.getAppFile() == null || version.getAppFile().isEmpty()) {
                errors.rejectValue(APP_FILE_FIELD, "applicationVersionValidator.emptyInstallFile");
            } else {
                validateExtension(version, parentApplicationType, errors);
            }
        }
    }

    protected void validateAppState(Errors errors, ApplicationVersionForm version) {
        if (version.getAppState() == null) {
            errors.rejectValue(APP_STATE_FIELD, "applicationVersionValidator.emptyAppState");
        }

        // Cannot set version to org publish through ApplicationVersionForm unless already set to org publish
        if (version.isEditing()) {
            ApplicationVersion currentVersion = applicationVersionService.get(version.getId());
            if(!AppState.ORGANIZATION_PUBLISH.equals(currentVersion.getAppState()) && AppState.ORGANIZATION_PUBLISH.equals(version.getAppState())) {
                errors.rejectValue(APP_STATE_FIELD, "applicationVersionValidator.invalidAppState");
            }
        } else {
            if(AppState.ORGANIZATION_PUBLISH.equals(version.getAppState())) {
                errors.rejectValue(APP_STATE_FIELD, "applicationVersionValidator.invalidAppState");
            }
        }
    }

    protected void validateRecentChanges(Errors errors, ApplicationVersionForm version) {
        if (version.getRecentChanges() == null || !StringUtils.hasText(version.getRecentChanges())) {
            errors.rejectValue(RECENT_CHANGES_FIELD, "applicationVersionValidator.emptyRecentChanges");
        }
    }

    protected void validateVersionName(Errors errors, ApplicationVersionForm version) {
        if (version.getVersionName() == null || !StringUtils.hasText(version.getVersionName())) {
            errors.rejectValue(VERSION_NAME_FIELD, "applicationVersionValidator.emptyVersion");
        } else if (!version.isEditing() && applicationVersionService.doesVersionExistForApplication(version.getParentId(), version.getVersionName())) {
            errors.rejectValue(VERSION_NAME_FIELD, "applicationVersionValidator.versionNameExists");
        }
    }

    private void validateApplicationVersionLimit(Application parentApplication, Organization organization, Errors errors) {
        if(organization.getDomainConfiguration().isDisabledDomain()) {
            errors.reject("applicationVersionValidator.group.disabled");
        }

        if(!organization.getDomainConfiguration().isDisableLimitValidations() && applicationService.isApplicationVersionLimit(parentApplication, organization)) {
            errors.reject("applicationVersionValidator.organization.limit");
        }
    }

    private void validateOrganizationStorageLimit(Organization organization, long installationFileSize, Errors errors) {
        if(organization.getDomainConfiguration().isDisableLimitValidations()) {
            return;
        }
        double installFileSizeMB = installationFileSize / MEGABYTE_CONVERSION;
        double totalAmount = organizationService.getTotalMegabyteStorageAmount(organization);
        long limit = organization.getDomainConfiguration().getMegabyteStorageLimit();
        if((totalAmount + installFileSizeMB) > limit) {
            errors.reject("applicationVersionValidator.organization.storageLimit");
        }
    }

    private void validateExtension(ApplicationVersionForm appVersion, ApplicationType parentApplicationType, Errors errors) {
        String fileName = appVersion.getAppFile().getOriginalFilename();
        boolean isValid = false;

        if (parentApplicationType.equals(ApplicationType.ANDROID)) {
            isValid = fileName.toLowerCase().endsWith(".apk");
        } else if (ApplicationType.IOS.equals(parentApplicationType) || parentApplicationType.equals(ApplicationType.IPAD) || parentApplicationType.equals(ApplicationType.IPHONE)) {
            isValid = fileName.toLowerCase().endsWith(".ipa");
        } /*else if (ApplicationType.CHROME.equals(parentApplicationType)) {
            isValid = fileName.toLowerCase().endsWith(".crx");
        } else if (ApplicationType.FIREFOX.equals(parentApplicationType)) {
            isValid = fileName.toLowerCase().endsWith(".xpi");
        } else if (ApplicationType.BLACKBERRY.equals(parentApplicationType)) {
            isValid = fileName.toLowerCase().endsWith(".jar") || fileName.toLowerCase().endsWith(".alx") ||
                    fileName.toLowerCase().endsWith(".zip") || fileName.toLowerCase().endsWith(".jad");
        } else if (ApplicationType.WINDOWSPHONE7.equals(parentApplicationType)) {
            isValid = fileName.toLowerCase().endsWith(".xap");
        } else if (ApplicationType.OTHER.equals(parentApplicationType)) {
            isValid = true;
        }*/

        if(!isValid) {
            errors.rejectValue(APP_FILE_FIELD, "applicationVersionValidator.invalidApplicationType");
        }
    }

    protected void validateResign(ApplicationVersionForm appVersion, Group parentGroup, ApplicationType parentApplicationType, Errors errors) {
        //Having the keyVaultEntry ID set specifies that the user wants the application resigned.
        if (appVersion.getKeyVaultEntryId() != null && appVersion.getKeyVaultEntryId() > 0) {
            KeyVaultEntry keyVaultEntry = keyVaultEntryService.get(appVersion.getKeyVaultEntryId());
            boolean isValid = false;
            if (keyVaultEntry != null) {

                //Check if keyVaultEntry parent domain matches the parent domain of the application version.
                if (keyVaultEntry.getParentDomain() != null && keyVaultEntry.getParentDomain().equals(parentGroup)) {
                    isValid = true;
                }

                if (!isValid) {
                    //Check if the parent domain of the application version matches any child domain of the keyVaultEntry.
                    for (Domain domain : keyVaultEntry.getChildDomains()) {
                        if (domain.equals(parentGroup)) {
                            isValid = true;
                            break;
                        }
                    }
                }

                //Check if KeyVaultEntry supports to correct applicationType
                if (isValid && !ApplicationType.getAllInGroup(keyVaultEntry.getApplicationType()).contains(parentApplicationType)) {
                    isValid = false;
                }
            }

            if (!isValid) {
                errors.rejectValue(KEY_VAULT_ENTRY_FIELD, "applicationVersionValidator.invalidKeyVaultEntry");
            }
        }
    }

    private String createErrorFieldName(String prefix, String fieldName) {
        return StringUtils.hasText(prefix) ? String.format("%s.%s", StringUtils.trimAllWhitespace(prefix), fieldName) : fieldName;
    }
}
