package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.NotifiableType;

/**
 * Mark an entity as notifiable if any change to the entity should fire off event messages.
 */
public interface Notifiable {

    /**
     * @return NotifiableType - the type of notifiable entity
     */
    NotifiableType getNotifiableType();

    /**
     * @return Long - the unique identifier of the entity
     */
    Long getId();
}
