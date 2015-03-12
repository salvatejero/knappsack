package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.DropBoxStorageConfiguration;
import com.sparc.knappsack.components.entities.S3StorageConfiguration;
import com.sparc.knappsack.components.entities.StorageConfiguration;
import com.sparc.knappsack.components.services.StorageConfigurationService;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.forms.StorageForm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component("storageConfigurationValidator")
public class StorageConfigurationValidator implements Validator {

    private static final String NAME_FIELD = "name";
    private static final String BASE_LOCATION_FIELD = "baseLocation";
    private static final String STORAGE_TYPE_FIELD = "storageType";
    private static final String BUCKET_NAME_FIELD = "bucketName";
    private static final String ACCESS_KEY_FIELD = "accessKey";
    private static final String SECRET_KEY_FIELD = "secretKey";

    @Qualifier("storageConfigurationService")
    @Autowired(required = true)
    private StorageConfigurationService storageConfigurationService;

    @Override
    public boolean supports(Class<?> clazz) {
        return StorageForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StorageForm storageForm = (StorageForm) target;
        validateStorage(storageForm, errors);
        if (StorageType.AMAZON_S3.equals(storageForm.getStorageType())) {
            validateS3Storage(storageForm, errors);
        }else if (StorageType.DROPBOX.equals(storageForm.getStorageType())) {
        	validateDropBoxStorage(storageForm, errors);
        }
    }

    private void validateStorage(StorageForm storageForm, Errors errors) {
        if (storageForm.isEditing()) {
            StorageConfiguration storageConfiguration = storageConfigurationService.get(storageForm.getId());
            if (storageConfiguration == null) {
                errors.reject("storageConfigurationValidator.generic.error");
            }
        }

        StorageConfiguration storageConfiguration = storageConfigurationService.getByName(storageForm.getName());
        if(storageConfiguration != null && !storageConfiguration.getId().equals(storageForm.getId())) {
            errors.rejectValue(NAME_FIELD, "storageConfigurationValidator.nameEquals");
        }

        if (storageForm.getName() == null || "".equals(storageForm.getName().trim())) {
            errors.rejectValue(NAME_FIELD, "storageConfigurationValidator.emptyName");
        }

        if (storageForm.getBaseLocation() == null || "".equals(storageForm.getBaseLocation().trim())) {
            errors.rejectValue(BASE_LOCATION_FIELD, "storageConfigurationValidator.emptyLocation");
        }

        if (storageForm.getStorageType() == null && !storageForm.isEditing()) {
            errors.rejectValue(STORAGE_TYPE_FIELD, "storageConfigurationValidator.emptyStorageType");
        }

        if(storageForm.isEditing() && storageConfiguration != null) {
            if(!storageConfiguration.getBaseLocation().equals(storageForm.getBaseLocation())) {
                storageForm.setBaseLocation(storageConfiguration.getBaseLocation());
                errors.rejectValue(BASE_LOCATION_FIELD, "storageConfigurationValidator.baseLocation.edited");
            }

            if(!storageConfiguration.getStorageType().equals(storageForm.getStorageType())) {
                storageForm.setStorageType(storageConfiguration.getStorageType());
                errors.rejectValue(STORAGE_TYPE_FIELD, "storageConfigurationValidator.storageType.edited");
            }
        }
    }



    private void validateS3Storage(StorageForm storageForm, Errors errors) {
        if(storageForm.isEditing()) {
            StorageConfiguration storageConfiguration = storageConfigurationService.get(storageForm.getId());
            if(storageConfiguration instanceof S3StorageConfiguration && !((S3StorageConfiguration)storageConfiguration).getBucketName().equals(storageForm.getBucketName())) {
                storageForm.setBucketName(((S3StorageConfiguration) storageConfiguration).getBucketName());
                errors.rejectValue(BUCKET_NAME_FIELD, "storageConfigurationValidator.bucketName.edited");
            }
        }

        if (!StringUtils.hasText(storageForm.getBucketName())) {
            errors.rejectValue(BUCKET_NAME_FIELD, "storageConfigurationValidator.bucketName.empty");
        }

        if (!StringUtils.hasText(storageForm.getAccessKey())) {
            errors.rejectValue(ACCESS_KEY_FIELD, "storageConfigurationValidator.accessKey.empty");
        }

        if (!StringUtils.hasText(storageForm.getSecretKey())) {
            errors.rejectValue(SECRET_KEY_FIELD, "storageConfigurationValidator.secretKey.empty");
        }
    }
    
    private void validateDropBoxStorage(StorageForm storageForm, Errors errors) {
        if(storageForm.isEditing()) {
            StorageConfiguration storageConfiguration = storageConfigurationService.get(storageForm.getId());
            if(storageConfiguration instanceof DropBoxStorageConfiguration && !((DropBoxStorageConfiguration)storageConfiguration).getBucketName().equals(storageForm.getBucketName())) {
                storageForm.setBucketName(((DropBoxStorageConfiguration) storageConfiguration).getBucketName());
                errors.rejectValue(BUCKET_NAME_FIELD, "storageConfigurationValidator.bucketName.edited");
            }
        }

        if (!StringUtils.hasText(storageForm.getBucketName())) {
            errors.rejectValue(BUCKET_NAME_FIELD, "storageConfigurationValidator.bucketName.empty");
        }

        if (!StringUtils.hasText(storageForm.getAccessKey())) {
            errors.rejectValue(ACCESS_KEY_FIELD, "storageConfigurationValidator.accessKey.empty");
        }

        if (!StringUtils.hasText(storageForm.getSecretKey())) {
            errors.rejectValue(SECRET_KEY_FIELD, "storageConfigurationValidator.secretKey.empty");
        }
    }
}

