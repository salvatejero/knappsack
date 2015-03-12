package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.enums.MimeType;
import com.sparc.knappsack.forms.KeyVaultEntryForm;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

@Component("keyVaultEntryValidator")
public class KeyVaultEntryValidator implements Validator {

    private static final String APPLICATION_TYPE_FIELD = "applicationType";
    private static final String NAME_FIELD = "name";
    private static final String DISTRIBUTION_CERT_FIELD = "distributionCert";
    private static final String DISTRIBUTION_KEY_FIELD = "distributionKey";
    private static final String DISTRIBUTION_PROFILE_FIELD = "distributionProfile";

    @Override
    public boolean supports(Class<?> clazz) {
        return KeyVaultEntryForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        KeyVaultEntryForm form = (KeyVaultEntryForm) target;

        boolean isEdit = (form.getId() != null && form.getId() > 0);

        if (!isEdit && (form.getApplicationType() == null || !form.getApplicationType().isKeyVaultCandidate())) {
            errors.rejectValue(APPLICATION_TYPE_FIELD, "keyVaultEntryValidator.invalidApplicationType");
        }

        if (!StringUtils.hasText(form.getName())) {
            errors.rejectValue(NAME_FIELD, "keyVaultEntryValidator.invalidName");
        }

        if (!errors.hasFieldErrors(APPLICATION_TYPE_FIELD) && !isEdit) {
            switch (form.getApplicationType()) {
                case IOS:
                    validateIOS(form, errors);
                    break;
                default:
                    errors.rejectValue(APPLICATION_TYPE_FIELD, "keyVaultEntryValidator.invalidApplicationType");
                    break;
            }
        }

    }

    private void validateIOS(KeyVaultEntryForm form, Errors errors) {
        if (form.getDistributionCert() == null || !validateMimeType(form.getDistributionCert(), MimeType.CERTIFICATE)) {
            errors.rejectValue(DISTRIBUTION_CERT_FIELD, "keyVaultEntryValidator.invalidDistributionCert");
        }

        if (form.getDistributionKey() == null || !validateMimeType(form.getDistributionKey(), MimeType.KEY)) {
            errors.rejectValue(DISTRIBUTION_KEY_FIELD, "keyVaultEntryValidator.invalidDistributionKey");
        }

        if (form.getDistributionProfile() == null || !validateMimeType(form.getDistributionProfile(), MimeType.MOBILE_PROVISIONING_PROFILE)) {
            errors.rejectValue(DISTRIBUTION_PROFILE_FIELD, "keyVaultEntryValidator.invalidDistributionProfile");
        }

        if (!StringUtils.hasText(form.getDistributionKeyPassword())) {
            errors.rejectValue("distributionKeyPassword", "keyVaultEntryValidator.invalidDistributionKeyPassword");
        }
    }

    private boolean validateMimeType(MultipartFile file, MimeType mimeType) {
        boolean isValid = false;

        if (file != null) {
            isValid = mimeType.getMimeType().equalsIgnoreCase(file.getContentType());
            if(!isValid){
            	MimeType certAux = MimeType.CERTIFICATE_AUX;
            	isValid = file.getOriginalFilename().endsWith(certAux.getExtension()) 
            			&& MimeType.CERTIFICATE_AUX.getMimeType().equalsIgnoreCase(file.getContentType());
            }
            if(!isValid){
            	MimeType certAux = MimeType.MOBILE_PROVISIONING_2;
            	isValid = file.getOriginalFilename().endsWith(certAux.getExtension()) 
            			&& MimeType.MOBILE_PROVISIONING_2.getMimeType().equalsIgnoreCase(file.getContentType());
            }
        }

        return isValid;
    }
}
