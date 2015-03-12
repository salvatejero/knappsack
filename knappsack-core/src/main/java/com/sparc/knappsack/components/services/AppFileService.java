package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.models.AppFileModel;
import com.sparc.knappsack.models.ImageModel;

import java.util.List;

public interface AppFileService extends EntityService<AppFile> {

    /**
     * @return List<AppFile> - get all AppFile entries in the system
     */
    List<AppFile> getAll();

    /**
     * @param appFile AppFile - delete this entity from existence
     */
    void delete(AppFile appFile);

    /**
     * @param appFile AppFile
     * @return String - retrieve the URL for this image
     */
    String getImageUrl(AppFile appFile);

    /**
     * @param appFile AppFile
     * @return ImageModel - an image model populate from the given AppFile
     */
    ImageModel createImageModel(AppFile appFile);

    /**
     * @param appFileId
     * @return
     */
    AppFileModel createAppFileModel(Long appFileId);
}
