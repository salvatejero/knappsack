package com.sparc.knappsack.components.services;

public interface EntityService<Entity> {
    /**
     * @param entity - persist a new Entity object to the database
     * @see Entity
     */
    public void add(Entity entity);

    /**
     * @param id Long - the primary key of the Entity
     * @return Entity with the given id
     * @see Entity
     */
    public Entity get(Long id);

    /**
     * @param id Long - removes this entity from the cache and database with this ID
     * @see Entity
     */
    public void delete(Long id);

    /**
     * @param entity Entity - merges changes to this Entity and persists the changes to the database
     * @see Entity
     */
    public void update(Entity entity);

    /**
     * Checks to see if an entity with the given Id exists
     * @param id Long - ID of the entity to check
     * @return whether the entity exists or not
     */
    public boolean doesEntityExist(Long id);
}
