package com.sparc.knappsack.components.entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jasypt.util.text.BasicTextEncryptor;

/**
 * S3StorageConfiguration contains the account settings in order to store files in Amazon's S3 storage service.
 * @see StorageConfiguration
 */
@Entity
@DiscriminatorValue("DROPBOX")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DropBoxStorageConfiguration extends StorageConfiguration {

    private static final long serialVersionUID = -4486931189704366819L;

    @Transient
    private static final String HASH_KEY = "3kLZX^ZF$B";

    @Transient
    private BasicTextEncryptor encryptor;

    @Column(name = "BUCKET_NAME")
    private String bucketName;

    @Column(name = "ACCESS_KEY")
    private String accessKey;

    @Column(name = "SECRET_KEY")
    private String secretKey;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return decrypt(this.secretKey);
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = encrypt(secretKey);
    }

    private String encrypt(String message) {
        return getEncryptor().encrypt(message);
    }

    private String decrypt(String message) {
        return getEncryptor().decrypt(message);
    }

    private BasicTextEncryptor getEncryptor() {
        if (encryptor == null) {
            encryptor = new BasicTextEncryptor();
            encryptor.setPassword(this.getUuid() + HASH_KEY);
        }
        return encryptor;
    }


}
