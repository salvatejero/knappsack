package com.sparc.knappsack.components.services;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.*;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppFileType;
import com.sparc.knappsack.enums.MimeType;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.forms.StorageForm;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jets3t.service.*;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.utils.RestUtils;
import org.jets3t.service.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.sparc.knappsack.properties.SystemProperties.*;

@Transactional( propagation = Propagation.REQUIRED )
@Service("s3StorageService")
@Scope("prototype")
public class S3StorageService extends AbstractStorageService implements RemoteStorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private static final String PATH_SEPARATOR = "/";
    private static final String CLOUDFRONT_DEV_PRIVATE_KEY_PATH = "security/cloudfront.dev.private.der";
    private static final String CLOUDFRONT_PROD_PRIVATE_KEY_PATH = "security/cloudfront.prod.private.der";
    private static final String CLOUDFRONT_DEV_KEY_PAIR_ID = "APKAJBVPEJS5R6N6BAXQ";
    private static final String CLOUDFRONT_PROD_KEY_PAIR_ID = "APKAIQSFJYOEJRPXBDKQ";
    private static final String PROD = "PROD";
    private static final String DEV = "DEV";

    private static boolean init = false;

    private static String cloudfrontKeyPairID;

    private static byte[] cloudfrontPrivateKey;

    @Value("${" + DYNAMODB_BANDWIDTH_TABLE + "}")
    private String bandwidthTableName;

    @Value("${" + CLOUDFRONT_URL + "}")
    private String cloudfrontURL = "";

    @Value("${" + AWS_ENVIRONMENT + "}")
    private String awsEnvironment = "";

    private static boolean cloudfrontEnabled = false;

    public String getPathSeparator() {
        return PATH_SEPARATOR;
    }

    private AppFile storeIcon(MultipartFile multipartFile, String key, Long storageConfigurationId) {
        long length;

        ByteArrayOutputStream outputStream = null;
        ByteArrayInputStream inputStream = null;
        try {
            try {
                outputStream = createThumbnail(multipartFile.getInputStream(), 72, 72);
                byte[] bytes = outputStream.toByteArray();
                length = bytes.length;
                inputStream = new ByteArrayInputStream(bytes);
            } catch (Exception e) {
                log.error("Exception creating thumbnail", e);
                saveMultipartFile(multipartFile, key, storageConfigurationId);
                return createAppFile(key, multipartFile);
            }

            writeToS3(inputStream, key + multipartFile.getOriginalFilename(), storageConfigurationId, length, multipartFile.getContentType());
            return createAppFile(key, multipartFile);
        } finally {
            closeInputStream(inputStream);
            closeOutputStream(outputStream);
        }
    }

    @Override
    protected StorageType getStorageType() {
        return StorageType.AMAZON_S3;
    }

    @Override
    public AppFile save(MultipartFile multipartFile, String appFileType, Long orgStorageConfigId, Long storageConfigurationId, String uuid) {
        if(multipartFile == null) {
            return null;
        }

        String key = getKey(orgStorageConfigId, appFileType, uuid);
        if (AppFileType.ICON.getPathName().equals(appFileType)) {
            return storeIcon(multipartFile, key, storageConfigurationId);
        } else {
            saveMultipartFile(multipartFile, key, storageConfigurationId);
            return createAppFile(key, multipartFile);
        }
    }

    private boolean saveMultipartFile(MultipartFile multipartFile, String key, Long storageConfigurationId) {

        InputStream is;
        try {
            is = multipartFile.getInputStream();
        } catch (IOException e) {
            log.error("IOException creating ByteArrayInputStream for multipart file: " + multipartFile.getOriginalFilename());
            return false;
        }

        String contentType = multipartFile.getContentType();
        MimeType mimeType = MimeType.getForFilename(multipartFile.getOriginalFilename());
        if (mimeType != null) {
            contentType = mimeType.getMimeType();
        }

        return writeToS3(is, key + multipartFile.getOriginalFilename(), storageConfigurationId, multipartFile.getSize(), contentType);
    }

    private String getKey(Long orgStorageConfigId, String appFileType, String  uuid) {
        OrgStorageConfig orgStorageConfig = orgStorageConfigService.get(orgStorageConfigId);
        return orgStorageConfig.getPrefix() + PATH_SEPARATOR + uuid + PATH_SEPARATOR + appFileType + PATH_SEPARATOR;
    }

    private boolean writeToS3(InputStream is, String key, Long storageConfigurationId, long contentLength, String contentType) {
        S3Object objectToSave = new S3Object(key);

        objectToSave.setDataInputStream(is);
        objectToSave.setContentLength(contentLength);
        objectToSave.setContentType(contentType);
        objectToSave.setServerSideEncryptionAlgorithm(S3Object.SERVER_SIDE_ENCRYPTION__AES256);

        S3StorageConfiguration storageConfiguration = getStorageConfiguration(storageConfigurationId);
        S3Service s3Service = getS3Service(storageConfiguration);
        String bucketName = storageConfiguration.getBucketName();
        try {
            s3Service.putObject(bucketName, objectToSave);
        } catch (S3ServiceException e) {
            log.error("S3ServiceException attempting to put object on bucket: " + bucketName + " Object: " + objectToSave.toString());
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                log.error("Unable to close stream:", e);
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
	protected S3StorageConfiguration getStorageConfiguration(Long storageConfigurationId) {
        return storageConfigurationService.get(storageConfigurationId, S3StorageConfiguration.class);
    }

    public boolean delete(AppFile appFile) {
        if (appFile == null) {
            return true;
        }
        S3StorageConfiguration storageConfiguration = (S3StorageConfiguration) appFile.getStorable().getStorageConfiguration();
        S3Service service = getS3Service(storageConfiguration);
        String bucketName = storageConfiguration.getBucketName();
        try {
            service.deleteObject(bucketName, appFile.getRelativePath());
        } catch (ServiceException e) {
            log.error("Error deleting object from bucket '" + bucketName + "'.  Object name: " + appFile.getName());
            return false;
        }

        return true;
    }

    protected S3Service getS3Service(S3StorageConfiguration storageConfiguration) {
        String awsAccessKey = storageConfiguration.getAccessKey();
        String awsSecretKey = storageConfiguration.getSecretKey();

        AWSCredentials awsCredentials = new AWSCredentials(awsAccessKey, awsSecretKey);
        S3Service s3Service;
        try {
            s3Service = new RestS3Service(awsCredentials);
        } catch (S3ServiceException e) {
            log.error("S3ServiceException attempting to create RestS3Service");
            return null;
        }
        return s3Service;
    }

    public String getUrl(AppFile appFile, int secondsToExpire) {
        //Determine what the time will be in the specified amount of seconds.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, secondsToExpire);
        Date expiryDate = cal.getTime();

        try {
            S3StorageConfiguration storageConfiguration = (S3StorageConfiguration) appFile.getStorable().getStorageConfiguration();

            if (cloudfrontEnabled) {
                String encodedUrlPath = RestUtils.encodeUrlPath(appFile.getRelativePath(), "/");

                //Determine when the object was last modified in S3 and append the timestamp to the URL to force cloudfront cache busting
                StorageObject storageObject = getS3Service(storageConfiguration).getObjectDetails(storageConfiguration.getBucketName(), appFile.getRelativePath());
                if (storageObject != null && storageObject.getLastModifiedDate() != null) {
                    encodedUrlPath += String.format("?%s", TimeUnit.MILLISECONDS.toSeconds(storageObject.getLastModifiedDate().getTime()));
                }

                return CloudFrontService.signUrlCanned(String.format("%s/%s", cloudfrontURL, encodedUrlPath), cloudfrontKeyPairID, cloudfrontPrivateKey, expiryDate);
            } else {
                return getS3Service(storageConfiguration).createSignedGetUrl(storageConfiguration.getBucketName(), appFile.getRelativePath(), expiryDate, false);
            }
        } catch (CloudFrontServiceException e) {
            log.error("CloudFrontServiceException attempting to create signed URL for file: " + appFile.getRelativePath(), e);
        } catch (ServiceException e) {
            log.error("ServiceException attempting to create signed URL for file: " + appFile.getRelativePath(), e);
        }

        return "";
    }

    public String buildPublicUrl(String path) {
        return ServletUriComponentsBuilder.fromHttpUrl(cloudfrontURL).path(path).build().toString();
    }

    @Override
    public double getMegabyteBandwidthUsed(OrgStorageConfig orgStorageConfig, Date start, Date end) {
        if (start.compareTo(end) > 0) {
            return 0;
        }
        S3StorageConfiguration storageConfiguration = (S3StorageConfiguration) BaseEntity.initializeAndUnproxy(orgStorageConfig.getStorageConfigurations().get(0));
        com.amazonaws.auth.AWSCredentials credentials = new BasicAWSCredentials(storageConfiguration.getAccessKey(), storageConfiguration.getSecretKey());
        AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(credentials);

        //TODO get hash key and range key names from table description
        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
        Condition hashCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(orgStorageConfig.getPrefix()));
        scanFilter.put("bucket_prefix", hashCondition);

        Condition rangeCondition = getRangeCondition(start, end);
        scanFilter.put("time", rangeCondition);

        ScanRequest scanRequest = new ScanRequest(bandwidthTableName).withScanFilter(scanFilter);
        ScanResult scanResult;
        try {
            scanResult = dynamoDB.scan(scanRequest);
        } catch (Exception e) {
            log.error("Error scanning DynamoDB table: " + bandwidthTableName, e);
            return 0;
        }

        log.info("DyanomoDB bandwidth result: " + scanResult);

        long totalBytesSent = 0;
        List<Map<String, AttributeValue>> resultItems = scanResult.getItems();
        for (Map<String, AttributeValue> resultItem : resultItems) {
            AttributeValue value = resultItem.get("total_bytes_sent");
            String bytesSentValue = value.getN();
            log.info("Bytes Sent: " + bytesSentValue);
            totalBytesSent += Long.parseLong(bytesSentValue);
        }

        return totalBytesSent / MEGABYTE_CONVERSION;
    }

    private Condition getRangeCondition(Date start, Date end) {
        Condition condition = null;
        if(start.compareTo(end) < 0) {
            condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.BETWEEN.toString())
                    .withAttributeValueList(new AttributeValue().withN(String.valueOf(start.getTime())), new AttributeValue().withN(String.valueOf(end.getTime())));
        } else if(start.compareTo(end) == 0) {
            condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue().withN(String.valueOf(start.getTime())));
        }

        return condition;
    }

    @Override
    public StorageConfiguration toStorageConfiguration(StorageForm storageForm) {
        S3StorageConfiguration s3StorageConfiguration = new S3StorageConfiguration();
        s3StorageConfiguration.setName(storageForm.getName());
        s3StorageConfiguration.setBaseLocation(storageForm.getBaseLocation());
        s3StorageConfiguration.setStorageType(StorageType.AMAZON_S3);
        s3StorageConfiguration.setBucketName(storageForm.getBucketName());
        s3StorageConfiguration.setAccessKey(storageForm.getAccessKey());
        s3StorageConfiguration.setSecretKey(storageForm.getSecretKey());
        s3StorageConfiguration.setRegistrationDefault(storageForm.isRegistrationDefault());
        return s3StorageConfiguration;
    }

    @Override
    public void mapFormToEntity(StorageForm form, StorageConfiguration entity) {
        if (form != null && entity != null) {
            entity.setName(form.getName().trim());
            entity.setRegistrationDefault(form.isRegistrationDefault());
            if (entity instanceof S3StorageConfiguration) {
                ((S3StorageConfiguration)entity).setAccessKey(form.getAccessKey());
                ((S3StorageConfiguration)entity).setSecretKey(form.getSecretKey());
            }
        }
    }

    @Override
    public InputStream getInputStream(AppFile appFile) {
        Assert.notNull(appFile, "AppFile cannot be null");
        S3StorageConfiguration storageConfiguration = (S3StorageConfiguration) appFile.getStorable().getStorageConfiguration();
        S3Service service = getS3Service(storageConfiguration);
        String bucketName = storageConfiguration.getBucketName();
        try {
            S3Object s3Object = service.getObject(bucketName, appFile.getRelativePath());
            if (s3Object != null) {
                return s3Object.getDataInputStream();
            }
        } catch (ServiceException e) {
            log.error("Error getting object from bucket '" + bucketName + "'.  Object name: " + appFile.getName());
            return null;
        }

        return null;
    }

    @PostConstruct
    private void init() {
        if (!init) {
            init = true;
            Security.addProvider(new BouncyCastleProvider());

            String prop = System.getProperty(CLOUDFRONT_ENABLED);
            if (StringUtils.hasText(prop)) {
                cloudfrontEnabled = Boolean.valueOf(StringUtils.trimAllWhitespace(prop)).booleanValue();
            }

            if (cloudfrontEnabled) {
                String keyPath;

                log.info(String.format("AWS Environment: %s", awsEnvironment));

                if (PROD.equalsIgnoreCase(awsEnvironment)) {
                    cloudfrontKeyPairID = CLOUDFRONT_PROD_KEY_PAIR_ID;
                    keyPath = CLOUDFRONT_PROD_PRIVATE_KEY_PATH;
                } else if (DEV.equalsIgnoreCase(awsEnvironment)) {
                    cloudfrontKeyPairID = CLOUDFRONT_DEV_KEY_PAIR_ID;
                    keyPath = CLOUDFRONT_DEV_PRIVATE_KEY_PATH;
                } else {
                    cloudfrontKeyPairID = CLOUDFRONT_DEV_KEY_PAIR_ID;
                    keyPath = CLOUDFRONT_DEV_PRIVATE_KEY_PATH;
                }

                log.info(String.format("CloudFront Key Pair ID: %s", cloudfrontKeyPairID));

                InputStream keyInputStream = this.getClass().getClassLoader().getResourceAsStream(keyPath);
                try {
                    cloudfrontPrivateKey = ServiceUtils.readInputStreamToBytes(keyInputStream);
                } catch (IOException e) {
                    log.error(String.format("Cannot read CloudFront Private Key (%s) from resources. Wrapping in unchecked exception.", keyPath), e);
                    init = false;
                    cloudfrontEnabled = false;
                    throw new IllegalStateException(String.format("Wrapped IOException while reading CloudFront Private Key (%s) from resources.", keyPath), e);
                }

                if (!StringUtils.hasText(cloudfrontKeyPairID) || cloudfrontPrivateKey == null || cloudfrontPrivateKey.length <= 0) {
                    cloudfrontEnabled = false;
                }
            }
        }
    }
}
