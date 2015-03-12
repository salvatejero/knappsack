package com.sparc.knappsack.components.services;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.util.WebRequest;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service("iosService")
public class IOSServiceImpl implements IOSService {

    public static final String DEFAULT_ICON_PATH = "/static/resources/img/default_icon.png";
    @Qualifier("applicationVersionService")
    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("storageServiceFactory")
    @Autowired(required = true)
    private StorageServiceFactory storageServiceFactory;

    @Override
    public String createIOSPlistXML(Long applicationVersionId, WebRequest webRequest, String token) {
        ApplicationVersion applicationVersion = applicationVersionService.get(applicationVersionId);

        if (applicationVersion == null) {
            return null;
        }

        String downloadUrl = createIOSDownloadIPAUrl(applicationVersion, webRequest, token);
//        String iconUrl = createIOSIconUrl(applicationVersion);

        NSDictionary nsDictionary = createIOSPlistNSDictionary(applicationVersion.getCfBundleIdentifier(), applicationVersion.getCfBundleVersion(), applicationVersion.getCfBundleName(), downloadUrl, null);

        String xmlPlist = null;
        if (nsDictionary != null) {
            xmlPlist = nsDictionary.toXMLPropertyList();
        }
        return xmlPlist;
    }

    private String createIOSDownloadIPAUrl(ApplicationVersion version, WebRequest request, String token) {
        AppFile appFile = version.getInstallationFile();

        StorageService storageService = storageServiceFactory.getStorageService(appFile.getStorageType());
        if (storageService instanceof RemoteStorageService) {
            return ((RemoteStorageService) storageService).getUrl(appFile, 14400 /*4 hours*/);
        }

        NameValuePair tokenParam = new BasicNameValuePair("token", token);
        return request.generateURL("/ios/downloadApplication/" + version.getId(), tokenParam);
    }

    private String createIOSIconUrl(ApplicationVersion version) {
        // Attempt to create icon URL if Version is not null
        if (version != null) {
            Application application = version.getApplication();
            // If application is not null then search for application icon
            if (application != null) {
                // If Application icon is not null then build url
                if (application.getIcon() != null) {

                    // Create Application icon URL based on icon StorageType
                    StorageService storageService = storageServiceFactory.getStorageService(application.getIcon().getStorageType());
                    if (storageService instanceof RemoteStorageService) {
                        return ((RemoteStorageService) storageService).getUrl(application.getIcon(), 120);
                    } else if (storageService instanceof LocalStorageService) {
                        return WebRequest.getInstance().generateURL("/image/" + application.getIcon().getId());
                    }
                } else {
                    // Create default icon URL since no icon is set on the application
                    return createDefaultIOSIconUrl(application.getStorageConfiguration().getStorageType());
                }
            } else {
                // Create default icon URL since the application for the version is null
                return createDefaultIOSIconUrl(version.getStorageConfiguration().getStorageType());
            }
        }

        // No icon URL could be created up until this point so attempt to create a default one
        return createDefaultIOSIconUrl(null);
    }

    private String createDefaultIOSIconUrl(StorageType storageType) {
        if (storageType != null) {
            // Create icon URL based on supplied StorageType

            StorageService storageService = storageServiceFactory.getStorageService(storageType);
            if (storageService instanceof RemoteStorageService) {
                return ((RemoteStorageService) storageService).buildPublicUrl(DEFAULT_ICON_PATH);
            } else if (storageService instanceof LocalStorageService) {
                // Only create icon URL is WebRequest instance exists
                if (WebRequest.getInstance() != null) {
                    return WebRequest.getInstance().generateURL(DEFAULT_ICON_PATH);
                }
            }
        } else {
            // StorageType was not supplied so attempt to create icon URL if WebRequest instance exists
            if (WebRequest.getInstance() != null) {
                return WebRequest.getInstance().generateURL(DEFAULT_ICON_PATH);
            }
        }

        // Could not create icon URL
        return "";
    }

    private NSDictionary createIOSPlistNSDictionary(String cfBundleIdentifier, String cfBundleVersion, String cfBundleName, String ipaUrl, String iconUrl) {
        NSDictionary mainDict = new NSDictionary();

        NSArray items = new NSArray(1);
        NSArray assets;
        if (StringUtils.hasText(iconUrl)) {
            assets = new NSArray(2);
        } else {
            assets = new NSArray(1);
        }
        NSDictionary itemsDict = new NSDictionary();

        NSDictionary assetsDict = new NSDictionary();
        assetsDict.put("kind", "software-package");
        assetsDict.put("url", ipaUrl);

        assets.setValue(0, assetsDict);

        if (StringUtils.hasText(iconUrl)) {
            NSDictionary iconsDict = new NSDictionary();
            iconsDict.put("kind", "display-image");
            iconsDict.put("needs-shine", true);
            iconsDict.put("url", "https://d1g1p4u8ho16cr.cloudfront.net/static/cb2639330650/resources/img/default_icon.png");

            assets.setValue(1, iconsDict);
        }

        NSDictionary mettadataDict = new NSDictionary();
        mettadataDict.put("bundle-identifier", cfBundleIdentifier);
        mettadataDict.put("bundle-version", cfBundleVersion);
        mettadataDict.put("kind", "software");
        mettadataDict.put("title", cfBundleName);

        itemsDict.put("assets", assets);
        itemsDict.put("metadata", mettadataDict);

        items.setValue(0, itemsDict);

        mainDict.put("items", items);

        return mainDict;
    }
}
