package com.tondise.utils.absrtractServices;

import com.tondise.utils.abstractModel.AbstractDTO;
import com.tondise.utils.abstractModel.AbstractEntity;
import com.tondise.utils.abstractModel.BaseRepository;
import com.tondise.utils.abstractModel.GenericSearchSpecification;
import com.tondise.utils.crud.CrudSupport;
import com.tondise.utils.abstractController.AbstractController;
import com.tondise.utils.crud.EntityFilterSpecification;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Socle CRUD historique, <b>par héritage</b>.
 *
 * <h2>État : figé</h2>
 * Cette classe reste en place et son contrat ne change pas — plus d'une centaine
 * de services en héritent. Elle n'évolue plus : <b>tout nouveau service doit
 * utiliser {@link CrudSupport}</b>, qui offre les mêmes opérations par
 * composition. Les deux modèles cohabitent sans se gêner.
 *
 * <h2>Ce qui a changé sous le capot</h2>
 * L'implémentation a été vidée au profit de {@link CrudSupport} et de
 * {@link EntityFilterSpecification} : cette classe n'est
 * plus qu'un <b>adaptateur</b> qui expose l'ancienne API. Il n'existe donc
 * qu'une seule implémentation du CRUD et du filtrage dynamique, corrigée à un
 * seul endroit — au lieu de deux versions vouées à diverger.
 *
 * <p>Bénéfice immédiat et sans migration pour tous les services héritiers :</p>
 * <ul>
 *   <li>le filtrage accepte désormais les colonnes de type {@link java.time.Instant}
 *       ({@code created}, {@code updated} depuis leur passage en
 *       {@code timestamptz}), qui échouaient auparavant ;</li>
 *   <li>les montants sont convertis directement en {@code BigDecimal}, sans
 *       passer par {@code Double} et sa perte de précision ;</li>
 *   <li>un type non filtrable renvoie une 400 explicite au lieu d'une 500.</li>
 * </ul>
 *
 * <h2>Limite non résolue ici</h2>
 * Le vrai risque de l'héritage n'est pas dans cette classe mais dans
 * {@link AbstractController}, qui
 * <b>publie des routes HTTP</b> que personne n'a écrites — dont un
 * {@code DELETE} sur chaque entité. Sortir un contrôleur de cet héritage reste
 * un geste délibéré, à faire entité par entité.
 */
@Slf4j
public abstract class AbstractService<T extends AbstractEntity, D extends AbstractDTO, K> {

    protected final BaseRepository<T> repository;
    private final CacheManager cacheManager;

    /** Délégué : porte l'implémentation réelle du CRUD. */
    private final CrudSupport<T, D> crud;

    @Getter
    protected final String entityName;

    // Convert model to DTO
    protected abstract D convertToDTO(T model);

    // Convert DTO to model
    protected abstract T convertToModel(D dto);

    protected AbstractService(BaseRepository<T> repository, CacheManager cacheManager, String entityName) {
        this.repository = repository;
        this.cacheManager = cacheManager;
        this.entityName = entityName;
        // Référence de méthode : convertToDTO n'est appelée qu'à l'usage, jamais
        // pendant la construction — l'implémentation de la sous-classe est donc
        // bien en place au moment où elle sert.
        this.crud = new CrudSupport<>(repository, this::convertToDTO, entityName);
    }

    /**
     * Create a collection
     *
     * @param dto
     */
    @Transactional
    public abstract D create(K dto);

    /**
     * find in collection by id
     * @param id
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "findById", keyGenerator = "customKeyGenerator")
    public D findById(UUID id) {
        return crud.findById(id);
    }

    /**
     * find all in collection
     * @return
     */
    @Cacheable(cacheNames = "findAll", keyGenerator = "customKeyGenerator")
    @Transactional(readOnly = true)
    public List<D> findAll() {
        return crud.findAll();
    }

    /**
     * update a line in collection
     * @param dto
     */
    @Transactional
    public abstract D update(K dto, UUID id);

    /**
     * Soft delete a line in collection
     * @param id
     * @return D
     */
    @Transactional
    public D softdelete(UUID id) {
        D deleted = crud.softDelete(id);
        this.clearEntityCache();
        return deleted;
    }

    /**
     * delete a row by Id
     * @param id
     */
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
        this.clearEntityCache();
    }

    /**
     * find all with pagination
     * @param page
     * @param size
     * @return
     */
    @Cacheable(cacheNames = "findAllPaged", keyGenerator = "customKeyGenerator")
    @Transactional(readOnly = true)
    public Page<D> findAllPaged(int page, int size) {
        return crud.findAllPaged(page, size);
    }

    @Cacheable(cacheNames = "count", keyGenerator = "customKeyGenerator")
    @Transactional(readOnly = true)
    public long count() {
        return crud.count();
    }

    /**
     * Find all entities with dynamic filtering and pagination
     *
     * @param page        The page number (0-based)
     * @param size        The page size
     * @param column      The column name for filtering
     * @param value       The value to filter with
     * @param operator    The operator to apply (contains, startsWith, endsWith, equals, greaterThan, lessThan, between)
     * @return A page of DTOs that match the filter criteria
     */
    @Cacheable(cacheNames = "findAllWithFilter", keyGenerator = "customKeyGenerator")
    @Transactional(readOnly = true)
    public Page<D> findAllWithFilter(int page, int size, String column, String value, String operator) {
        return crud.findAllWithFilter(page, size, column, value, operator);
    }

    public static <T> Specification<T> notDeleted() {
        return CrudSupport.notDeleted();
    }

    /**
     * Vide les caches partagés.
     *
     * <p>⚠️ {@code invalidate()} vide <b>toute la région</b>, pas la seule entrée
     * de cette entité : les régions ({@code findById}, {@code findAll}…) sont
     * communes à tous les services héritiers. Écrire sur une entité vide donc le
     * cache de toutes les autres. C'est volontairement conservé — une éviction
     * plus fine risquerait de laisser des données dérivées périmées, ce qui est
     * bien plus grave sur des données financières qu'un cache vidé trop souvent.
     * Les services migrés vers {@link CrudSupport} déclarent des régions
     * nominatives et n'ont pas ce comportement.</p>
     */
    public void clearEntityCache() {
        String entityName = this.entityName;

        // List of cache names and method names
        Map<String, String> cacheMethodMap = Map.of(
                "findById", "findById",
                "findAll", "findAll",
                "findAllPaged", "findAllPaged",
                "findAllWithFilter", "findAllWithFilter",
                "count", "count",
                "searchByRelation", "searchByRelation"
        );

        for (Map.Entry<String, String> entry : cacheMethodMap.entrySet()) {
            String cacheName = entry.getKey();
            String methodName = entry.getValue();

            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                String keyPrefix = entityName + "-" + methodName;
                cache.invalidate();
                log.info("✅ Cleared cache '{}' for key prefix '{}'", cacheName, keyPrefix);
            } else {
                log.warn("⚠️ Cache '{}' not found", cacheName);
            }
        }
    }


    public void clearCache() {
        clearEntityCache();
    }

    @Cacheable(cacheNames = "searchByRelation", keyGenerator = "searchByRelation")
    @Transactional(readOnly = true)
    public Page<D> searchByRelation(UUID applicationProductId, String keyword, int page, int size, String relationName) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<T> spec = GenericSearchSpecification.searchByKeywordAndAppId(
                applicationProductId,
                keyword,
                relationName,     // ex: "applicationProduct"
                getEntityClass()
        );

        Page<T> result = repository.findAll(spec, pageable);
        return result.map(this::convertToDTO);
    }

    /**
     * Get the class of the entity for MongoTemplate operations.
     * Needs to be implemented by the subclass.
     */
    protected abstract Class<T> getEntityClass();

}
