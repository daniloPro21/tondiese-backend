package com.tondise.utils.abstractController;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Opérations HTTP exposées par {@link AbstractController}.
 *
 * <p>Une classe mère annotée {@code @DeleteMapping} publie la route chez
 * <b>toutes</b> ses sous-classes : l'héritage rend l'exposition invisible, et
 * c'est ainsi qu'un {@code DELETE} s'est retrouvé ouvert sur des soldes et sur
 * un journal comptable sans que personne ne l'ait décidé.</p>
 *
 * <p>Un contrôleur déclare ce qu'il autorise en redéfinissant
 * {@link AbstractController#allowedOperations()}. Les opérations non déclarées
 * répondent <b>405 Method Not Allowed</b> avec un message explicite.</p>
 *
 * <pre>{@code
 * @Override
 * protected Set<CrudOperation> allowedOperations() {
 *     return CrudOperation.NO_HARD_DELETE;   // tout sauf la suppression physique
 * }
 * }</pre>
 */
public enum CrudOperation {

    /** {@code POST /} — création. */
    CREATE,
    /** {@code GET /}, {@code GET /{id}}, {@code GET /all}, {@code GET /count} — lecture. */
    READ,
    /** {@code PUT /{id}} — mise à jour. ⚠️ remplace l'entité par le corps reçu. */
    UPDATE,
    /** {@code PUT /softdelete/{id}} — suppression logique, réversible et auditable. */
    SOFT_DELETE,
    /** {@code DELETE /{id}} — <b>suppression physique, irréversible</b>. */
    DELETE,
    /** {@code GET /findWithFilter} — filtrage dynamique. */
    FILTER,
    /** {@code GET /search} — recherche par relation. */
    SEARCH;

    /**
     * Toutes les opérations. C'est le comportement historique, et le défaut :
     * aucun contrôleur existant ne change de surface tant qu'il n'a rien déclaré.
     */
    public static final Set<CrudOperation> ALL =
            Collections.unmodifiableSet(EnumSet.allOf(CrudOperation.class));

    /**
     * Tout sauf la suppression physique — le réglage attendu pour les données
     * financières et les pistes d'audit, qui se corrigent par une écriture
     * inverse et non par un {@code DELETE}.
     */
    public static final Set<CrudOperation> NO_HARD_DELETE =
            Collections.unmodifiableSet(EnumSet.complementOf(EnumSet.of(DELETE)));

    /** Lecture seule : consultation, filtrage et recherche uniquement. */
    public static final Set<CrudOperation> READ_ONLY =
            Collections.unmodifiableSet(EnumSet.of(READ, FILTER, SEARCH));
}
