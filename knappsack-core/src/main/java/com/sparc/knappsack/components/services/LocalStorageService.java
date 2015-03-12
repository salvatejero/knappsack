package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.LocalStorageConfiguration;
import com.sparc.knappsack.components.entities.OrgStorageConfig;
import com.sparc.knappsack.components.entities.StorageConfiguration;
import com.sparc.knappsack.enums.AppFileType;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.forms.StorageForm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@Transactional( propagation = Propagation.REQUIRED )
@Service("localStorageService")
@Scope("prototype")
public class LocalStorageService extends AbstractStorageService implements StorageService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String PATH_SEPARATOR = System.getProperty("file.separator");

    @Override
    public String getPathSeparator() {
        return PATH_SEPARATOR;
    }

    private boolean saveMultipartFile(MultipartFile multipartFile, File file) {
        InputStream inputStream = null;
        try {
            if (multipartFile != null && multipartFile.getSize() > 0 && file != null) {
                return writeToLocal(multipartFile, file);
            }
            return true;
        } finally {
            if(inputStream != null) {
                closeInputStream(inputStream);
            }
        }
    }

    private boolean writeToLocal(MultipartFile multipartFile, File file) {
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            log.error("IOException writing to file.", e);
            return false;
        }

        return true;
    }

    private boolean writeToLocal(InputStream inputStream, File file, long fileSize, String originalFilename) {
        FileOutputStream outputStream = null;
        try {
            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                log.error("FileNotFoundException for file named: " + originalFilename, e);
                return false;
            }

            int readBytes;
            try {
                byte[] buffer = new byte[(int) fileSize];
                while ((readBytes = inputStream.read(buffer, 0, (int) fileSize)) != -1) {
                    outputStream.write(buffer, 0, readBytes);
                }
            } catch (IOException e) {
                log.error("IOException for file named: " + originalFilename, e);
                return false;
            }

            return true;
        } finally {
            closeInputStream(inputStream);
            closeOutputStream(outputStream);
        }
    }

    private File getFile(String path, MultipartFile multipartFile) {
        File file = new File(path);
        createDirectories(file);
        String filePath = path + multipartFile.getOriginalFilename();
        file = new File(filePath);
        createFile(file);

        return file;
    }

    private boolean createDirectories(File file) {
        return file.exists() || file.mkdirs();
    }


    private boolean createFile(File file) {
        try {
            return file.exists() || file.createNewFile();
        } catch (IOException e) {
            log.error("IOException caught creating new file", e);
            return false;
        }
    }

    public boolean delete(AppFile appFile) {
        if (appFile == null) {
            return true;
        }

        //Delete the actual file
        String path = appFile.getAbsolutePath();
        deletePath(path);

        String basePath = path.replace(appFile.getRelativePath(), "");

        //Get the directory names for the app file
        StringTokenizer st = new StringTokenizer(appFile.getRelativePath(), PATH_SEPARATOR);
        List<String> relativeDirectories = new ArrayList<String>();
        while(st.hasMoreTokens()) {
            relativeDirectories.add(st.nextToken());
        }

        //Loop through parent directories and deletePath any that are empty.
        for (int x = 0; x < relativeDirectories.size(); x++) {
            path = basePath + getPathSeparator() + StringUtils.arrayToDelimitedString(relativeDirectories.subList(0, relativeDirectories.size() - x).toArray(), getPathSeparator());
            if(path.equals(appFile.getStorable().getStorageConfiguration().getBaseLocation())) {
                break;
            }


            //TODO: handle deletion of directories even though mac .DS_Store file is present
            try {
                deletePath(path);
            } catch (IllegalArgumentException e) {
                log.error("An error occurred deleting the path: " + path, e);
            }
        }

        return true;
    }

    private boolean deletePath(String path) {
        File file = new File(path);

        if (!file.exists()) {
            return true;
        }

        if (!file.canWrite()) {
            throw new IllegalArgumentException("Delete: write protected: " + path);
        }

        // If it is a directory, make sure it is empty
        if (file.isDirectory()) {
            String[] files = file.list();
            if (files.length > 0) {
                throw new IllegalArgumentException("Delete: directory not empty: " + path);
            }
        }

        return file.delete();
    }

    @Override
    public AppFile save(MultipartFile multipartFile, String appFileType, Long orgStorageConfigId, Long storageConfigurationId, String uuid) {
        if(multipartFile == null) {
            return null;
        }

        String path = getInitialPath(storageConfigurationId, orgStorageConfigId) + PATH_SEPARATOR + uuid + PATH_SEPARATOR + appFileType + PATH_SEPARATOR;
        String relativePath = getPrefix(orgStorageConfigId) + PATH_SEPARATOR + uuid + PATH_SEPARATOR + appFileType + PATH_SEPARATOR;
        File file = getFile(path, multipartFile);
        if (AppFileType.ICON.getPathName().equals(appFileType)) {
            return storeIcon(multipartFile, file, relativePath);
        } else {
            saveMultipartFile(multipartFile, file);
            return createAppFile(relativePath, multipartFile);
        }
    }

    private AppFile storeIcon(MultipartFile multipartFile, File file, String path) {
        ByteArrayOutputStream outputStream = null;
        ByteArrayInputStream inputStream = null;
        long length;

        try {
            try {
                outputStream = createThumbnail(multipartFile.getInputStream(), 72, 72);
                byte[] bytes = outputStream.toByteArray();
                length = bytes.length;
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            } catch (Exception e) {
                log.error("Exception creating thumbnail: ", e);
                saveMultipartFile(multipartFile, file);
                return createAppFile(path, multipartFile);
            }
            writeToLocal(inputStream, file, length, multipartFile.getOriginalFilename());
            return createAppFile(path, multipartFile);
        } finally {
            closeInputStream(inputStream);
            closeOutputStream(outputStream);
        }
    }

    @SuppressWarnings("unchecked")
	protected LocalStorageConfiguration getStorageConfiguration(Long storageConfigurationId) {
        return storageConfigurationService.get(storageConfigurationId, LocalStorageConfiguration.class);
    }

    private String getInitialPath(Long storageConfigurationId, Long orgStorageConfigId) {
        LocalStorageConfiguration storageConfiguration = getStorageConfiguration(storageConfigurationId);
        return storageConfiguration.getBaseLocation() + getPrefix(orgStorageConfigId);
    }

    private String getPrefix(Long orgStorageConfigId) {
        OrgStorageConfig orgStorageConfig = orgStorageConfigService.get(orgStorageConfigId);
        return PATH_SEPARATOR + orgStorageConfig.getPrefix();
    }

    @Override
    protected StorageType getStorageType() {
        return StorageType.LOCAL;
    }

    @Override
    public StorageConfiguration toStorageConfiguration(StorageForm storageForm) {
        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setName(storageForm.getName());
        localStorageConfiguration.setBaseLocation(storageForm.getBaseLocation());
        localStorageConfiguration.setStorageType(StorageType.LOCAL);
        localStorageConfiguration.setRegistrationDefault(storageForm.isRegistrationDefault());
        return localStorageConfiguration;
    }

    @Override
    public void mapFormToEntity(StorageForm form, StorageConfiguration entity) {
        if (form != null && entity != null) {
            entity.setName(form.getName().trim());
            entity.setRegistrationDefault(form.isRegistrationDefault());
        }
    }

    @Override
    public InputStream getInputStream(AppFile appFile) {
        Assert.notNull(appFile, "AppFile cannot be null");

        try {
            return new FileInputStream(appFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            log.error(String.format("Unable to create FileInputStream for appFile with ID %s", appFile.getId()));
        }

        return null;
    }
}
