package au.com.mountainpass.hyperstate.core;

import java.util.concurrent.CompletableFuture;

import org.springframework.data.repository.Repository;
import org.springframework.scheduling.annotation.Async;

import au.com.mountainpass.hyperstate.core.entities.DeletedEntity;
import au.com.mountainpass.hyperstate.core.entities.EntityWrapper;

public interface EntityRepository extends Repository<EntityWrapper<?>, String> {

    /**
     * Deletes a given entity.
     *
     * @param entity
     *            the entity to deleted
     * @return And empty future to allow execution when the delete completes.
     * @throws IllegalArgumentException
     *             in case the given entity is {@literal null}.
     */
    @Async
    CompletableFuture<DeletedEntity> delete(EntityWrapper<?> entity);

    /**
     * Deletes the entity with the given id.
     *
     * @param id
     *            must not be {@literal null}.
     * @return And empty future to allow execution when the delete completes.
     * @throws IllegalArgumentException
     *             in case the given {@code id} is {@literal null}
     *
     *
     */
    @Async
    CompletableFuture<DeletedEntity> delete(String id);

    /**
     * Returns whether an entity with the given id exists.
     *
     * @param id
     *            must not be {@literal null}.
     * @return true if an entity with the given id exists, {@literal false}
     *         otherwise
     * @throws IllegalArgumentException
     *             if {@code id} is {@literal null}
     */

    @Async
    CompletableFuture<Boolean> exists(String id);

    /**
     * Retrieves an entity by its id.
     *
     * @param id
     *            must not be {@literal null}.
     * @return the entity with the given id or {@literal null} if none found
     * @throws IllegalArgumentException
     *             if {@code id} is {@literal null}
     */
    @Async
    CompletableFuture<EntityWrapper<?>> findOne(String id);

    @Async
    public <S extends EntityWrapper<?>> CompletableFuture<S> findOne(
            String path, Class<S> type);

    /**
     * Saves a given entity. Use the returned instance for further operations as
     * the save operation might have changed the entity instance completely.
     *
     * @param entity
     *            The entity to be saved.
     * @return the saved entity
     * @param <S>
     *            the type of the entity being saved
     */
    @Async
    <S extends EntityWrapper<?>> CompletableFuture<S> save(S entity);

    @Async
    CompletableFuture<Void> deleteAll();

}
