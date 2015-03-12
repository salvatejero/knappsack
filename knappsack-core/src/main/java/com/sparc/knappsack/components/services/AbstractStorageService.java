package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.StorageConfiguration;
import com.sparc.knappsack.enums.MimeType;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.exceptions.ImageException;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(AbstractStorageService.class);

    protected static final double MEGABYTE_CONVERSION = 1048576;

    @Qualifier("storageConfigurationService")
    @Autowired(required = true)
    protected StorageConfigurationService storageConfigurationService;

    @Qualifier("orgStorageConfigService")
    @Autowired(required = true)
    protected OrgStorageConfigService orgStorageConfigService;

    protected abstract StorageType getStorageType();

    protected final AppFile createAppFile(String path, MultipartFile multipartFile) {
        AppFile file = new AppFile();
        file.setName(multipartFile.getOriginalFilename());

        String contentType = multipartFile.getContentType();
        MimeType mimeType = MimeType.getForFilename(multipartFile.getOriginalFilename());
        if (mimeType != null) {
            contentType = mimeType.getMimeType();
        }
        file.setType(contentType);

        double megabyteSize = multipartFile.getSize() / MEGABYTE_CONVERSION;
        file.setSize(megabyteSize);
        file.setRelativePath(path + multipartFile.getOriginalFilename());
        file.setStorageType(getStorageType());

        return file;
    }

    protected ByteArrayOutputStream createThumbnail(InputStream inputStream, int width, int height) throws ImageException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            BufferedImage originalImage = ImageIO.read(inputStream);
            if(originalImage != null) {
                int originalImageWidth = originalImage.getWidth();
                int originalImageHeight = originalImage.getHeight();

                if (originalImageWidth > width || originalImageHeight > height) {
                    inputStream.reset();
                    Thumbnails.of(inputStream)
                            .size(width, height)
                            .keepAspectRatio(true)
                            .useOriginalFormat()
                            .toOutputStream(outputStream);

                } else {
                    throw new ImageException(String.format("Image resize not necessary. Already %d x %d", originalImageWidth, originalImageHeight));
                }
            }
        } catch (IOException e) {
            throw new ImageException(e.getMessage(), e);
        } finally {
            closeInputStream(inputStream);
        }

        return outputStream;
    }

    protected void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("Unable to close InputStream:", e);
            }
        }
    }

    protected void closeOutputStream(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                log.error("Unable to close OutputStream:", e);
            }
        }
    }

    protected abstract <T extends StorageConfiguration> T getStorageConfiguration(Long id);
}
