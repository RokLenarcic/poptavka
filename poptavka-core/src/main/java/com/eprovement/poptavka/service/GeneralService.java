package com.eprovement.poptavka.service;

import com.googlecode.genericdao.search.ExampleOptions;
import com.googlecode.genericdao.search.Filter;
import com.googlecode.genericdao.search.ISearch;
import com.googlecode.genericdao.search.SearchResult;

import java.io.Serializable;
import java.util.List;

/**
 * Class that provides general methods withou the need to create a concrete subclass as it is in case of
 * {@link GenericService}.
 *
 * <p>
 *     This is only "wrapper" service for {@link com.googlecode.genericdao.dao.jpa.GeneralDAO}.
 *
 * @author Juraj Martinka
 *         Date: 3.5.11
 */
public interface GeneralService {

    /**
     * <p>
     * Get the entity with the specified type and id from the datastore.
     *
     * <p>
     * If none is found, return null.
     */
    <T> T find(Class<T> type, Serializable id);

    /**
     * Get all entities of the specified type from the datastore that have one
     * of these ids. An array of entities is returned that matches the same
     * order of the ids listed in the call. For each entity that is not found in
     * the datastore, a null will be inserted in its place in the return array.
     */
    <T> T[] find(Class<T> type, Serializable... ids);

    /**
     * <p>
     * Get a reference to the entity with the specified type and id from the
     * datastore.
     *
     * <p>
     * This does not require a call to the datastore and does not populate any
     * of the entity's values. Values may be fetched lazily at a later time.
     * This increases performance if a another entity is being saved that should
     * reference this entity but the values of this entity are not needed.
     *
     * @throws a
     *             HibernateException if no matching entity is found
     */
    <T> T getReference(Class<T> type, Serializable id);

    /**
     * <p>
     * Get a reference to the entities of the specified type with the given ids
     * from the datastore. An array of entities is returned that matches the
     * same order of the ids listed in the call.
     *
     * <p>
     * This does not require a call to the datastore and does not populate any
     * of the entities' values. Values may be fetched lazily at a later time.
     * This increases performance if a another entity is being saved that should
     * reference these entities but the values of these entities are not needed.
     *
     * @throws a
     *             HibernateException if any of the matching entities are not
     *             found.
     */
    <T> T[] getReferences(Class<T> type, Serializable... ids);

    /**
     * <p>
     * Make a transient instance persistent and add it to the datastore. This
     * operation cascades to associated instances if the association is mapped
     * with cascade="persist". Throws an error if the entity already exists.
     *
     * <p>
     * Does not guarantee that the object will be assigned an identifier
     * immediately. With <code>persist</code> a datastore-generated id may not
     * be pulled until flush time.
     */
    void persist(Object... entities);

    /**
     * <p>
     * Copy the state of the given object onto the persistent object with the
     * same identifier. If there is no persistent instance currently associated
     * with the session, it will be loaded. Return the persistent instance. If
     * the given instance is unsaved, save a copy and return it as a newly
     * persistent instance.
     *
     * <p>
     * The instance that is passed in does not become associated with the
     * session. This operation cascades to associated instances if the
     * association is mapped with cascade="merge".
     */
    <T> T merge(T entity);

    /**
     * <p>
     * Copy the state of the given objects onto the persistent objects with the
     * same identifier. If there is no persistent instance currently associated
     * with the session, it will be loaded. Return the persistent instances. If
     * a given instance is unsaved, save a copy and return it as a newly
     * persistent instance.
     *
     * <p>
     * The instances that are passed in do not become associated with the
     * session. This operation cascades to associated instances if the
     * association is mapped with cascade="merge".
     */
    Object[] merge(Object... entities);

    /**
     * If an entity with the same ID already exists in the database, merge the
     * changes into that entity. If not persist the given entity. In either
     * case, a managed entity with the changed values is returned. It may or may
     * not be the same object as was passed in.
     */
    <T> T save(T entity);

    /**
     * <p>
     * For each entity: If an entity with the same ID already exists in the
     * database, merge the changes into that entity. If not persist the given
     * entity. In either case, a managed entity with the changed values is
     * returned. It may or may not be the same object as was passed in.
     *
     * @return an array containing each managed entity corresponding to the
     *         entities passed in.
     */
    Object[] save(Object... entities);

    /**
     * Remove the specified entity from the datastore.
     *
     * @return <code>true</code> if the entity is found in the datastore and
     *         removed, <code>false</code> if it is not found.
     */
    boolean remove(Object entity);

    /**
     * Remove all of the specified entities from the datastore.
     */
    void remove(Object... entities);

    /**
     * Remove the entity with the specified type and id from the datastore.
     *
     * @return <code>true</code> if the entity is found in the datastore and
     *         removed, <code>false</code> if it is not found.
     */
    boolean removeById(Class<?> type, Serializable id);

    /**
     * Remove all the entities of the given type from the datastore that have
     * one of these ids.
     */
    void removeByIds(Class<?> type, Serializable... ids);

    /**
     * Get a list of all the objects of the specified type.
     */
    <T> List<T> findAll(Class<T> type);

    /**
     * Search for objects given the search parameters in the specified
     * <code>ISearch</code> object.
     */
    @SuppressWarnings("unchecked")
    List search(ISearch search);

    /**
     * Search for a single result using the given parameters.
     */
    Object searchUnique(ISearch search);

    /**
     * Returns the total number of results that would be returned using the
     * given <code>ISearch</code> if there were no paging or maxResults limits.
     */
    int count(ISearch search);

    /**
     * Returns a <code>SearchResult</code> object that includes both the list of
     * results like <code>search()</code> and the total length like
     * <code>count()</code>.
     */
    @SuppressWarnings("unchecked")
    SearchResult searchAndCount(ISearch search);

    /**
     * Returns <code>true</code> if the object is connected to the current
     * Hibernate session.
     */
    boolean isAttached(Object entity);

    /**
     * Refresh the content of the given entity from the current datastore state.
     */
    void refresh(Object... entities);

    /**
     * Flushes changes in the Hibernate session to the datastore.
     */
    void flush();

    /**
     * Generates a search filter from the given example using default options.
     */
    Filter getFilterFromExample(Object example);

    /**
     * Generates a search filter from the given example using the specified
     * options.
     */
    Filter getFilterFromExample(Object example, ExampleOptions options);

}
