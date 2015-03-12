package com.sparc.knappsack.components.services;

import static com.sparc.knappsack.properties.SystemProperties.CLOUDFRONT_ENABLED;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWriteMode;
import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.DropBoxStorageConfiguration;
import com.sparc.knappsack.components.entities.OrgStorageConfig;
import com.sparc.knappsack.components.entities.S3StorageConfiguration;
import com.sparc.knappsack.components.entities.StorageConfiguration;
import com.sparc.knappsack.enums.AppFileType;
import com.sparc.knappsack.enums.MimeType;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.forms.StorageForm;

@Transactional(propagation = Propagation.REQUIRED)
@Service("dropboxStorageService")
@Scope("prototype")
public class DropboxStorageService extends AbstractStorageService implements
		RemoteStorageService {

	private static final Logger log = LoggerFactory
			.getLogger(DropboxStorageService.class);

	private static final String PATH_SEPARATOR = "/";

	private static boolean init = false;
	
	private static boolean cloudfrontEnabled = false;
	

	private AppFile storeIcon(MultipartFile multipartFile, String key,
			Long storageConfigurationId) {
		long length;

		ByteArrayOutputStream outputStream = null;
		ByteArrayInputStream inputStream = null;
		try {
			try {
				outputStream = createThumbnail(multipartFile.getInputStream(),
						72, 72);
				byte[] bytes = outputStream.toByteArray();
				length = bytes.length;
				inputStream = new ByteArrayInputStream(bytes);
			} catch (Exception e) {
				//log.error("Exception creating thumbnail", e);
				saveMultipartFile(multipartFile, key, storageConfigurationId);
				return createAppFile(key, multipartFile);
			}

			writeToDropbox(inputStream,
					key + multipartFile.getOriginalFilename(),
					storageConfigurationId, length,
					multipartFile.getContentType());
			return createAppFile(key, multipartFile);
		} finally {
			closeInputStream(inputStream);
			closeOutputStream(outputStream);
		}
	}

	private boolean saveMultipartFile(MultipartFile multipartFile, String key,
			Long storageConfigurationId) {

		InputStream is;
		try {
			is = multipartFile.getInputStream();
		} catch (IOException e) {
			log.error("IOException creating ByteArrayInputStream for multipart file: "
					+ multipartFile.getOriginalFilename());
			return false;
		}

		String contentType = multipartFile.getContentType();
		MimeType mimeType = MimeType.getForFilename(multipartFile
				.getOriginalFilename());
		if (mimeType != null) {
			contentType = mimeType.getMimeType();
		}

		return writeToDropbox(is, key + multipartFile.getOriginalFilename(),
				storageConfigurationId, multipartFile.getSize(), contentType);
	}

	private boolean writeToDropbox(InputStream is, String key,
			Long storageConfigurationId, long contentLength, String contentType) {
		
		DropBoxStorageConfiguration storageConfiguration = getStorageConfiguration(storageConfigurationId);
		DbxClient dbService = getDropBoxService(storageConfiguration);
		String bucketName = storageConfiguration.getBucketName();
		try {
			if(!key.startsWith("/")){
				key = "/"+key;
			}
			 dbService.uploadFile(key,
					DbxWriteMode.add(), contentLength, is);
		} catch (DbxException e) {
			log.error("DbxException attempting to put object on bucket: "
					+ bucketName + " Object: " + key.toString());
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error io");
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

	@Override
	public AppFile save(MultipartFile multipartFile, String appFileType,
			Long orgStorageConfigId, Long storageConfigurationId, String uuid) {

		if (multipartFile == null) {
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

	private String getKey(Long orgStorageConfigId, String appFileType,
			String uuid) {
		OrgStorageConfig orgStorageConfig = orgStorageConfigService
				.get(orgStorageConfigId);
		return orgStorageConfig.getPrefix() + PATH_SEPARATOR + uuid
				+ PATH_SEPARATOR + appFileType + PATH_SEPARATOR;
	}

	@Override
	public boolean delete(AppFile appFile) {
		if (appFile == null) {
            return true;
        }
        DropBoxStorageConfiguration storageConfiguration = (DropBoxStorageConfiguration) appFile.getStorable().getStorageConfiguration();
        DbxClient service = getDropBoxService(storageConfiguration);
        String bucketName = storageConfiguration.getBucketName();
        try {
        	String path = appFile.getRelativePath();
            if(!path.startsWith("/")){
            	path = "/"+path;
            }
            service.delete(path);
        } catch (Exception e) {
            log.error("Error deleting object from bucket '" + bucketName + "'.  Object name: " + appFile.getName());
            return false;
        }

        return true;
	}

	@Override
	public String getPathSeparator() {
		return PATH_SEPARATOR;
	}

	@Override
	public StorageConfiguration toStorageConfiguration(StorageForm storageForm) {

		DropBoxStorageConfiguration dropBoxConfiguration = new DropBoxStorageConfiguration();
		dropBoxConfiguration.setName(storageForm.getName());
		dropBoxConfiguration.setBaseLocation(storageForm.getBaseLocation());
		dropBoxConfiguration.setStorageType(StorageType.DROPBOX);
		dropBoxConfiguration.setBucketName(storageForm.getBucketName()
				.replaceAll(",", ""));
		dropBoxConfiguration.setAccessKey(storageForm.getAccessKey()
				.replaceAll(",", ""));
		dropBoxConfiguration.setSecretKey(storageForm.getSecretKey()
				.replaceAll(",", ""));
		dropBoxConfiguration.setRegistrationDefault(storageForm
				.isRegistrationDefault());
		return dropBoxConfiguration;
	}

	@Override
	public void mapFormToEntity(StorageForm form, StorageConfiguration entity) {

		if (form != null && entity != null) {
			entity.setName(form.getName().trim());
			entity.setRegistrationDefault(form.isRegistrationDefault());
			if (entity instanceof S3StorageConfiguration) {
				((DropBoxStorageConfiguration) entity).setAccessKey(form
						.getAccessKey());
				((DropBoxStorageConfiguration) entity).setSecretKey(form
						.getSecretKey());
			}
		}

	}

	@Override
	public InputStream getInputStream(AppFile appFile) {
		Assert.notNull(appFile, "AppFile cannot be null");
		DropBoxStorageConfiguration storageConfiguration = (DropBoxStorageConfiguration) appFile
				.getStorable().getStorageConfiguration();
		DbxClient client = getDropBoxService(storageConfiguration);

		try {
			ByteArrayOutputStream downBodyStream = new ByteArrayOutputStream();

			client.getFile(
					"/" + appFile.getName(), null, downBodyStream);

			byte[] content = downBodyStream.toByteArray();
			int size = content.length;
			InputStream is = null;
			byte[] b = new byte[size];
			try {
				is = new ByteArrayInputStream(content);
				is.read(b);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (is != null)
						is.close();
				} catch (Exception ex) {

				}
			}
			return is;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (DbxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public String getUrl(AppFile appFile, int secondsToExpire) {
		
		Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, secondsToExpire);

        try {
            DropBoxStorageConfiguration storageConfiguration = (DropBoxStorageConfiguration) appFile.getStorable().getStorageConfiguration();

            /*if (cloudfrontEnabled) {
                String encodedUrlPath = RestUtils.encodeUrlPath(appFile.getRelativePath(), "/");

                //Determine when the object was last modified in S3 and append the timestamp to the URL to force cloudfront cache busting
                StorageObject storageObject = getDropBoxService(storageConfiguration).getObjectDetails(storageConfiguration.getBucketName(), appFile.getRelativePath());
                if (storageObject != null && storageObject.getLastModifiedDate() != null) {
                    encodedUrlPath += String.format("?%s", TimeUnit.MILLISECONDS.toSeconds(storageObject.getLastModifiedDate().getTime()));
                }

                return CloudFrontService.signUrlCanned(String.format("%s/%s", cloudfrontURL, encodedUrlPath), cloudfrontKeyPairID, cloudfrontPrivateKey, expiryDate);
            } else {*/
            String path = appFile.getRelativePath();
            if(!path.startsWith("/")){
            	path = "/"+path;
            }
            return getDropBoxService(storageConfiguration).createTemporaryDirectUrl( path).url;
            //}
        } catch(DbxException e){
        	e.printStackTrace();
        }

        return "";
	}

	@Override
	public String buildPublicUrl(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getMegabyteBandwidthUsed(OrgStorageConfig orgStorageConfig,
			Date start, Date end) {

		return 2000000 / MEGABYTE_CONVERSION;
	}

	@Override
	protected StorageType getStorageType() {
		return StorageType.DROPBOX;
	}

	@SuppressWarnings("unchecked")
	protected DropBoxStorageConfiguration getStorageConfiguration(
			Long storageConfigurationId) {
		return storageConfigurationService.get(storageConfigurationId,
				DropBoxStorageConfiguration.class);
	}

	protected DbxClient getDropBoxService(
			DropBoxStorageConfiguration storageConfiguration) {

		DbxRequestConfig config = new DbxRequestConfig("knappsack", Locale
				.getDefault().toString());
		
		DbxClient client = new DbxClient(config, storageConfiguration.getBaseLocation());//
		return client;
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

           
        }
    }
}
