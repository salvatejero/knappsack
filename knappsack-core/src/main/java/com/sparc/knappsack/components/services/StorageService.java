package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.StorageConfiguration;
import com.sparc.knappsack.forms.StorageForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {

    /**
     * @param multipartFile MultipartFile - the data we want to persist to a specific storage mechanism
     * @param appFileType AppFileType - the type of file that the multipart file represents
     * @param storageConfigurationId - the id of the storage configuration used for storing the multipart file
     * @param uuid String - the uuid used for storage
     * @return AppFile - an representation of the MultipartFile.  It only contains information about the multipart file, not the data itself.
     */
    AppFile save(MultipartFile multipartFile, String appFileType, Long orgStorageConfigId, Long storageConfigurationId, String uuid);

    /**
     * @param appFile AppFile - Delete this entity and the data associated with it from storage.
     * @return boolean TRUE if the AppFile and data were successfully deleted.
     */
    boolean delete(AppFile appFile);

    /**
     * @return String - path separator use while saving files to storage service
     */
    String getPathSeparator();

    StorageConfiguration toStorageConfiguration(StorageForm storageForm);

    void mapFormToEntity(StorageForm form, StorageConfiguration entity);

    InputStream getInputStream(AppFile appFile);

}
