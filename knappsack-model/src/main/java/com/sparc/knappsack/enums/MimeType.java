package com.sparc.knappsack.enums;

import org.apache.commons.io.FilenameUtils;

public enum MimeType {
    ANDROID("apk", "application/vnd.android.package-archive", ContentType.APPLICATION),
    IOS("ipa", "application/octet-stream", ContentType.APPLICATION),
    CHROME("crx", "application/x-chrome-extension", ContentType.APPLICATION),
    FIREFOX("xpi", "application/x-xpinstall", ContentType.APPLICATION),
    WINPHONE("xap", "application/x-silverlight-app", ContentType.APPLICATION),
    BLACKBERRY_COD("cod", "application/vnd.rim.cod", ContentType.APPLICATION),
    BLACKBERRY_JAD("jad", "text/vnd.sun.j2me.app-descriptor", ContentType.TEXT),
    PNG("png", "image/png", ContentType.IMAGE),
    JPG("jpg", "image/jpeg", ContentType.IMAGE),
    JPEG("jpeg", "image/jpeg", ContentType.IMAGE),
    CERTIFICATE("cer", "application/x-x509-ca-cert", ContentType.APPLICATION),
    KEY("p12", "application/x-pkcs12", ContentType.APPLICATION),
    MOBILE_PROVISIONING_PROFILE("mobileprovision", "application/octet-stream", ContentType.APPLICATION),
    CERTIFICATE_AUX("cer", "application/octet-stream", ContentType.APPLICATION),
    MOBILE_PROVISIONING_2("mobileprovision", "application/x-apple-aspen-mobileprovision", ContentType.APPLICATION);

    private final String extension;
    private final String mimeType;
    private final ContentType contentType;

    MimeType(String extension, String mimeType, ContentType contentType) {
        this.extension = extension;
        this.mimeType = mimeType;
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public static ContentType getContentTypeForMimeType(String mimeType) {
        for (MimeType type : MimeType.values()) {
            if (type.getMimeType().equalsIgnoreCase(mimeType)) {
                return type.getContentType();
            }
        }
        return null;
    }

    public static MimeType getForFilename(String filename) {
        for (MimeType mimeType : MimeType.values()) {
            if (mimeType.getExtension().equalsIgnoreCase(FilenameUtils.getExtension(filename))) {
                return mimeType;
            }
        }
        return null;
    }
}
