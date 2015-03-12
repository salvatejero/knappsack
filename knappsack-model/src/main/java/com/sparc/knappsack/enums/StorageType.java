package com.sparc.knappsack.enums;

public enum StorageType {
    //Stored on the local server
    LOCAL("storageType.local", false),
    //Amazon's S3 storage service
    AMAZON_S3("storageType.amazonS3", true),

    DROPBOX("storageType.dropbox", true);
    //The message properties key for this StorageType
    private final String messageKey;
    //Specifies if the StorageType is not the server running Knappsack
    private final boolean remote;

    private StorageType(String messageKey, boolean remote) {
        this.messageKey = messageKey;
        this.remote = remote;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public boolean isRemote() {
        return remote;
    }
}
