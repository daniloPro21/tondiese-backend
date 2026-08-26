package com.tondise.utils.abstractController;

import com.tondise.utils.absrtractServices.AbstractService;
import com.tondise.utils.abstractModel.AbstractDTO;
import com.tondise.utils.abstractModel.AbstractEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Socle REST par héritage : expose le CRUD d'une entité.
 *
 * <h2>Le piège de l'héritage, et sa parade</h2>
 * Une classe mère annotée {@code @DeleteMapping} publie la route chez
 * <b>toutes</b> ses sous-classes. L'exposition d'une opération destructrice
 * devient donc invisible : elle n'apparaît dans aucun fichier écrit par un
 * développeur. C'est ainsi qu'un {@code DELETE} s'est retrouvé ouvert sur des
 * soldes applicatifs et sur un journal comptable.
 *
 * <p>Un contrôleur déclare désormais ce qu'il autorise en redéfinissant
 * {@link #allowedOperations()} ; les opérations non déclarées répondent
 * <b>405</b>. Exemple :</p>
 *
 * <pre>{@code
 * @Override
 * protected Set<CrudOperation> allowedOperations() {
 *     return CrudOperation.NO_HARD_DELETE;
 * }
 * }</pre>
 *
 * <p>⚠️ Le défaut reste {@link CrudOperation#ALL} : plus d'une centaine de
 * contrôleurs héritent de cette classe, et restreindre par défaut casserait des
 * usages légitimes (administration des données de référence). La protection
 * n'est donc effective que sur les contrôleurs qui l'ont déclarée — commencer
 * par les entités financières et les pistes d'audit.</p>
 */
@Slf4j
public abstract class AbstractController<T extends AbstractEntity, D extends AbstractDTO, K> {


    private final AbstractService<T, D, K> service;

    protected AbstractController(AbstractService<T, D, K> service) {
        this.service = service;
    }

    /**
     * Opérations que ce contrôleur expose.
     *
     * <p>Par défaut {@link CrudOperation#ALL}, pour ne modifier la surface
     * d'aucun contrôleur existant. À redéfinir dès que l'entité ne doit pas tout
     * accepter — voir {@link CrudOperation#NO_HARD_DELETE} et
     * {@link CrudOperation#READ_ONLY}.</p>
     */
    protected Set<CrudOperation> allowedOperations() {
        return CrudOperation.ALL;
    }

    /**
     * Refuse l'opération si le contrôleur ne l'a pas déclarée.
     *
     * <p>La route reste enregistrée et répond 405 : un 404 laisserait croire à
     * une erreur de chemin, alors que le refus est délibéré.</p>
     */
    private void requireAllowed(CrudOperation operation) {
        if (!allowedOperations().contains(operation)) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                    "Opération " + operation + " non autorisée sur cette ressource.");
        }
    }

    @Operation(summary = "Create", description = "Adds the system")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entity created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<D> create(@Valid  @RequestBody K dto) {
        requireAllowed(CrudOperation.CREATE);
        return ResponseEntity.ok(service.create(dto));
    }

    @Operation(summary = "Get by ID", description = "Fetch from the system using its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entity found"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<D> getById(@PathVariable UUID id) {
        requireAllowed(CrudOperation.READ);
        D entity = service.findById(id);
        return ResponseEntity.ok(entity);
    }

    @Operation(summary = "Get all", description = "Fetch all from the system")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entities retrieved successfully")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<D>> getAll() {
        requireAllowed(CrudOperation.READ);
        log.info("request executer");
        return ResponseEntity.ok(service.findAll());
    }


    @Operation(summary = "Find all", description = "Find all with  pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entity fetch successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<D> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        requireAllowed(CrudOperation.READ);
        return service.findAllPaged(page, size);
    }

    @Operation(summary = "Count all", description = "Count all")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entity Count successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public long count() {
        return service.count();
    }

    @Operation(summary = "Update an existing", description = "Update using its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entity updated successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<D> update(@Valid @PathVariable UUID id, @RequestBody K dto) {
        requireAllowed(CrudOperation.UPDATE);
        D updatedEntity = service.update(dto, id);
        return ResponseEntity.ok(updatedEntity);
    }

    @Operation(summary = "Soft delete", description = "Soft delete  using its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entity Soft delete successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @PutMapping(value = "softdelete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<D> softdelete(@PathVariable UUID id) {
        requireAllowed(CrudOperation.SOFT_DELETE);
        D updatedEntity = service.softdelete(id);
        return ResponseEntity.ok(updatedEntity);
    }

    @Operation(summary = "Delete by ID", description = "Delete from the system using its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Entity deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        requireAllowed(CrudOperation.DELETE);
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Find all with filter", description = "Find all  with filter and pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entity fetch successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    /**
     * Fetch all entities with dynamic filtering and pagination
     *
     * @param page     The page number (default is 0)
     * @param size     The size of each page (default is 10)
     * @param column   The column to filter by (optional)
     * @param value    The value to filter by (optional)
     * @param operator The operator to use for filtering (optional, default is "contains")
     * @return A page of filtered and paginated entities
     */
    @GetMapping(value = "/findWithFilter")
    public ResponseEntity<Page<D>> findAllWithFilter(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String column,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "contains") String operator
    ) {
        requireAllowed(CrudOperation.FILTER);
        // Fetch data from the service with pagination and optional filters
        Page<D> result = service.findAllWithFilter(page, size, column, value, operator);

        // Return paginated response
        return ResponseEntity.ok(result);
    }

    /**
     * Filter Table by Relation Name
     * @param page
     * @param size
     * @param keyWord
     * @param relationName
     * @param appId
     * @return
     */
    @Operation(summary = "Search by relation", description = "Recherche paginée par mot-clé sur une relation donnée de l'entité.")
    @GetMapping(value = "/search")
    public ResponseEntity<Page<D>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String keyWord,
            @RequestParam String relationName,
            @RequestParam UUID appId
    ) {
        requireAllowed(CrudOperation.SEARCH);
        // Fetch data from the service with pagination and optional filters
        Page<D> result = service.searchByRelation(appId, keyWord, page, size, relationName);

        // Return paginated response
        return ResponseEntity.ok(result);
    }
}
