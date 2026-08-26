package com.tondise.utils.crud;

import com.tondise.utils.abstractModel.AbstractEntity;
import com.tondise.utils.abstractModel.BaseRepository;
import com.tondise.utils.exception.ResourceNotFoundException;
import com.tondise.utils.absrtractServices.AbstractService;
import com.tondise.utils.abstractController.AbstractController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Socle CRUD <b>par composition</b> : un service le <i>détient</i> au lieu de
 * l'<i>étendre</i>.
 *
 * <h2>Pourquoi</h2>
 * {@link AbstractService} mutualise le même
 * code, mais par héritage. Trois conséquences observées en production :
 * <ul>
 *   <li>un service hérite de <b>toutes</b> les opérations, y compris celles qui
 *       n'ont aucun sens pour lui — c'est ainsi qu'un {@code DELETE} sur un
 *       solde et sur une écriture comptable s'est retrouvé exposé, via
 *       {@link AbstractController}, sans que
 *       personne ne l'ait décidé ;</li>
 *   <li>chaque service injecte <b>deux fois</b> son repository : le
 *       {@code BaseRepository<T>} réclamé par la classe mère, et le sien ;</li>
 *   <li>la moindre correction dans la classe mère impose une nouvelle version
 *       de cette librairie, redéployée par tous les services qui en héritent.</li>
 * </ul>
 *
 * <h2>Coexistence</h2>
 * Cette classe <b>ne remplace pas</b> {@code AbstractService} : les deux
 * cohabitent. Les services existants restent inchangés ; les nouveaux services,
 * et ceux migrés au fil de l'eau, utilisent la composition.
 *
 * <h2>Exemple</h2>
 * <pre>{@code
 * @Service
 * public class LogService {
 *     private final CrudSupport<Log, LogDto> crud;
 *
 *     public LogService(LogRepository repository, LogMapper mapper) {
 *         this.crud = new CrudSupport<>(repository, mapper::toDto, "Log");
 *     }
 *
 *     @Transactional(readOnly = true)
 *     public LogDto findById(UUID id) { return crud.findById(id); }
 *     // pas de méthode delete écrite → aucune suppression possible
 * }
 * }</pre>
 *
 * <h2>Ce qui change concrètement</h2>
 * Le service n'expose que ce qu'il délègue explicitement. Ne pas écrire de
 * méthode {@code delete} suffit à ce qu'aucune suppression ne soit possible :
 * la sécurité devient le défaut, et non plus une redéfinition à ne pas oublier.
 *
 * <h2>Transactions et cache</h2>
 * Cette classe ne porte volontairement <b>aucune</b> annotation
 * {@code @Transactional} ni {@code @Cacheable} : ce n'est pas un bean Spring,
 * donc aucun proxy ne les appliquerait. C'est le <b>service appelant</b> qui
 * les déclare — ce qui rend la mise en cache d'un {@code findAll()} sur une
 * table volumineuse impossible par inadvertance.
 *
 * @param <T> l'entité
 * @param <D> le DTO de sortie
 */
public class CrudSupport<T extends AbstractEntity, D> {

    private final BaseRepository<T> repository;
    private final Function<T, D> toDto;
    private final String entityName;

    public CrudSupport(BaseRepository<T> repository, Function<T, D> toDto, String entityName) {
        this.repository = repository;
        this.toDto = toDto;
        this.entityName = entityName;
    }

    /** Entité par identifiant, ou 404. */
    public T requireEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName + " introuvable pour l'id " + id));
    }

    /** DTO par identifiant, ou 404. */
    public D findById(UUID id) {
        return toDto.apply(requireEntity(id));
    }

    /**
     * Toutes les lignes, sans pagination.
     *
     * <p>⚠️ À réserver aux tables de référence (services, frais, opérateurs).
     * Sur une table de transactions, préférer {@link #findAllPaged(int, int)} :
     * cette méthode charge l'intégralité de la table en mémoire.</p>
     */
    public List<D> findAll() {
        return repository.findAll().stream().map(toDto).toList();
    }

    /** Page triée du plus récent au plus ancien. */
    public Page<D> findAllPaged(int page, int size) {
        return repository.findAllByOrderByCreatedDesc(PageRequest.of(page, size)).map(toDto);
    }

    public long count() {
        return repository.count();
    }

    /** Filtre dynamique {@code (colonne, valeur, opérateur)} reçu de l'URL. */
    public Page<D> findAllWithFilter(int page, int size, String column, String value, String operator) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<T> specification = EntityFilterSpecification.of(column, value, operator);
        return repository.findAll(specification, pageable).map(toDto);
    }

    /**
     * Suppression logique : la ligne reste en base et reste auditable.
     * Aucune suppression physique n'est exposée ici — s'est délibéré.
     */
    public D softDelete(UUID id) {
        T entity = requireEntity(id);
        entity.setDeleted(true);
        return toDto.apply(repository.save(entity));
    }

    /** Filtre « non supprimé », à combiner avec d'autres spécifications. */
    public static <T> Specification<T> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }
}
