package com.sparc.knappsack.components.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * A LocalStorageConfiguration is a StorageConfiguration where all the files are stored on a local server.
 * @see StorageConfiguration
 */
@Entity
@DiscriminatorValue("LOCAL")
public class LocalStorageConfiguration extends StorageConfiguration {

    private static final long serialVersionUID = 3991712048302311841L;
}
