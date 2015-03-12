package com.sparc.knappsack.components.services;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.googlecode.flyway.core.util.StringUtils;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppFileType;
import com.sparc.knappsack.models.SQSResignerModel;
import com.sparc.knappsack.properties.SystemProperties;
import com.sparc.knappsack.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.util.List;

public abstract class AbstractKeyVaultService<T extends KeyVaultEntry> implements KeyVaultService<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractKeyVaultService.class);

    @Qualifier("storageServiceFactory")
    @Autowired(required = true)
    private StorageServiceFactory storageServiceFactory;

    @Qualifier("appFileService")
    @Autowired(required = true)
    private AppFileService appFileService;

    private AmazonSQS sqsClient;

    @Value("${" + SystemProperties.KNAPPSACK_ACCESS_KEY + "}")
    private String awsAccessKey;

    @Value("${" + SystemProperties.KNAPPSACK_SECRET_KEY + "}")
    private String awsSecretKey;

    @Value("${" + SystemProperties.SQS_RESIGN_QUEUE + "}")
    private String sqsResignQueue;

    private String resignQueueUrl;

    protected StorageService getStorageService(Domain domain) {
        StorageService storageService = null;
        if (domain != null) {
            StorageConfiguration storageConfiguration = getStorageConfiguration(domain);
            if (storageConfiguration != null) {
                storageService = storageServiceFactory.getStorageService(storageConfiguration.getStorageType());
            }
        }
        return storageService;
    }

    protected StorageConfiguration getStorageConfiguration(Domain domain) {
        StorageConfiguration storageConfiguration = null;
        if (domain != null) {
            List<StorageConfiguration> storageConfigurations = domain.getStorageConfigurations();
            if (storageConfigurations != null && storageConfigurations.size() > 0) {
                storageConfiguration = storageConfigurations.get(0);
            }
        }
        return storageConfiguration;
    }

    protected AppFile saveFile(MultipartFile file, Domain domain, KeyVaultEntry keyVaultEntry) {
        AppFile appFile = null;
        if (file != null && domain != null && keyVaultEntry != null) {
            OrgStorageConfig orgStorageConfig = domain.getOrgStorageConfig();

            StorageConfiguration storageConfiguration = null;
            if (orgStorageConfig != null && orgStorageConfig.getStorageConfigurations() != null && orgStorageConfig.getStorageConfigurations().size() > 0) {
                storageConfiguration = orgStorageConfig.getStorageConfigurations().get(0);
            }

            StorageService storageService = null;
            if (storageConfiguration != null) {
                storageService = storageServiceFactory.getStorageService(storageConfiguration.getStorageType());
            }

            if (storageService != null && orgStorageConfig != null && storageConfiguration != null) {
                appFile = storageService.save(file, AppFileType.KEY_VAULT_ENTRY.getPathName(), orgStorageConfig.getId(), storageConfiguration.getId(), keyVaultEntry.getUuid());
                appFile.setStorable(keyVaultEntry);
            }
        }

        return appFile;
    }

    protected String getBucketName(ApplicationVersion applicationVersion) {
        String bucketName = null;
        if (applicationVersion != null && applicationVersion.getStorageConfiguration() != null && applicationVersion.getStorageConfiguration() instanceof S3StorageConfiguration) {
            bucketName = ((S3StorageConfiguration) applicationVersion.getStorageConfiguration()).getBucketName();
        }
        return bucketName;
    }

    protected boolean resign(SQSResignerModel model) {
        boolean success = false;
        if (model != null) {
            String json = JsonUtil.marshall(model);
            if (StringUtils.hasText(json)) {
                SendMessageRequest request = new SendMessageRequest(resignQueueUrl, json);
                try {
                    SendMessageResult result = sqsClient.sendMessage(request);
                    if (result != null) {
                        success = true;
                    }
                } catch (AmazonClientException e) {
                    log.error("Error sending resigning message to SQS: ", e);
                }
            }
        }
        return success;
    }

    protected void deleteAppFile(AppFile appFile) {
        if (appFile != null) {
            appFileService.delete(appFile);
        }
    }

    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    public void setSqsResignQueue(String sqsResignQueue) {
        this.sqsResignQueue = sqsResignQueue;
    }

    @PostConstruct
    private void postConstruct() {
        try {
            sqsClient = new AmazonSQSClient(new BasicAWSCredentials(awsAccessKey, awsSecretKey));

            GetQueueUrlRequest request = new GetQueueUrlRequest(sqsResignQueue);
            this.resignQueueUrl = sqsClient.getQueueUrl(request).getQueueUrl();
        } catch (AmazonServiceException ex) {
            log.error(String.format("Error constructing AbstractKeyVaultService.", ex));
        }
    }

}
