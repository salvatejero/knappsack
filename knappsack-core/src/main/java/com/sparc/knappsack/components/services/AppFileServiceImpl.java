package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.AppFileDao;
import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.StorageConfiguration;
import com.sparc.knappsack.models.AppFileModel;
import com.sparc.knappsack.models.ImageModel;
import com.sparc.knappsack.util.WebRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional( propagation = Propagation.REQUIRED )
@Service("appFileService")
public class AppFileServiceImpl implements AppFileService {

    @Qualifier("appFileDao")
    @Autowired(required=true)
    private AppFileDao appFileDao;

    @Qualifier("storageServiceFactory")
    @Autowired
    private StorageServiceFactory storageServiceFactory;

    @Qualifier("storageConfigurationService")
    @Autowired(required = true)
    protected StorageConfigurationService storageConfigurationService;

    @Override
    public void add(AppFile appFile) {
        appFileDao.add(appFile);
    }

    @Override
    public List<AppFile> getAll() {
        return appFileDao.getAll();
    }

    @Override
    public AppFile get(Long id) {
        return appFileDao.get(id);
    }

    @Override
    public void delete(Long id) {
        delete(get(id));
    }

    @Override
    public void update(AppFile appFile) {
        appFileDao.update(appFile);
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

    @Override
    public void delete(AppFile appFile) {
        if (appFile != null) {
            StorageService storageService = getStorageService(appFile.getStorable().getStorageConfiguration().getId());
            storageService.delete(appFile);
            appFile.setStorable(null);
            appFileDao.delete(appFile);
        }
    }

    @Override
    public String getImageUrl(AppFile appFile) {
        if (appFile != null) {
            StorageService storageService = storageServiceFactory.getStorageService(appFile.getStorageType());
            if (storageService instanceof RemoteStorageService) {
                return ((RemoteStorageService) storageService).getUrl(appFile, 120);
            } else if (storageService instanceof LocalStorageService) {
                return WebRequest.getInstance().generateURL("/image/" + appFile.getId());
            }
        }

        return "";
    }

    @Override
    public ImageModel createImageModel(AppFile appFile) {
        ImageModel imageModel = null;
        if (appFile != null) {
            imageModel = new ImageModel();
            imageModel.setId(appFile.getId());
            imageModel.setUrl(getImageUrl(appFile));
        }

        return imageModel;
    }

    @Override
    public AppFileModel createAppFileModel(Long appFileId) {
        AppFileModel model = null;
        AppFile appFile = get(appFileId);
        if (appFile != null) {
            model = new AppFileModel();
            model.setId(appFile.getId());
            model.setName(appFile.getName());
            model.setRelativePath(appFile.getRelativePath());
            model.setSize(appFile.getSize());
            model.setStorageType(appFile.getStorageType());
            model.setType(appFile.getType());
        }
        return model;
    }

    private StorageService getStorageService(Long storageConfigurationId) {
        return storageServiceFactory.getStorageService(getStorageConfiguration(storageConfigurationId).getStorageType());
    }

    private StorageConfiguration getStorageConfiguration(Long storageConfigurationId) {
        return storageConfigurationService.get(storageConfigurationId);
    }

}
