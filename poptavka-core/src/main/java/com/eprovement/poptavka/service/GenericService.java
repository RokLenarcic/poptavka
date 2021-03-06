
package com.eprovement.poptavka.service;

import com.eprovement.poptavka.dao.GenericDao;
import com.eprovement.poptavka.domain.common.DomainObject;
import com.eprovement.poptavka.domain.common.ResultCriteria;
import java.util.List;

public interface GenericService<T extends DomainObject, Dao extends GenericDao<T>> {
/**
     * get the entity with the given id.
     * @param id
     * @return entity
     * @throws com.eprovement.poptavka.exception.DomainObjectNotFoundException if no object is found
     */
    T getById(long id);

    /**
     * searches the entity with given id.
     * @param id
     * @return entity, null if none is found
     */
    T searchById(long id);

    /**
     * returns all entities.
     * @return
     */
    List<T> getAll();

    /**
     * Load all entities that satisfy additional criteria - this could be limit on max number of result and
     * similar restrictions.
     * <p>
     *     For more information:
     *     @see ResultCriteria
     * @return all entities that resultCriteria
     */
    List<T> getAll(ResultCriteria resultCriteria);

    /**
     * persists the entity.
     * @param entity
     * @return
     */
    T create(T entity);

    /**
     * Updates the entity.
     * @param entity
     * @return
     */
    T update(T entity);

    /**
     * Removes a persisted entity.
     * @param entity
     */
    void remove(T entity);

    /**
     * Refreshes the entity from database.
     * @param entity
     * @return
     */
    T refresh(T entity);

    /**
     * removes entity with given id.
     * @param id
     */
    void removeById(long id);

    Dao getDao();

    void setDao(Dao dao);


    /**
     * Gets the count of all the records of the associated entity
     * @return count of the records
     */
    long getCount();
}
