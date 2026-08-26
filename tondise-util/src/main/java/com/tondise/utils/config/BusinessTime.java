package com.tondise.utils.config;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Conversions entre les dates saisies par l'utilisateur et les {@link Instant}
 * stockés en base.
 *
 * <h2>Le piège qu'elle supprime</h2>
 * Depuis la migration des colonnes d'audit en {@code timestamptz}, les entités
 * portent des {@link Instant} tandis que les API reçoivent des
 * {@link LocalDate}/{@link LocalDateTime}. La conversion directe est
 * <b>impossible</b> et échoue à l'exécution :
 *
 * <pre>
 * Instant.from(localDateTime)   → DateTimeException: Unable to obtain Instant
 *                                  from TemporalAccessor ... UnsupportedTemporalTypeException:
 *                                  Unsupported field: InstantSeconds
 * LocalDateTime.from(instant)   → même échec, dans l'autre sens
 * </pre>
 *
 * <p>La raison est la même dans les deux sens : un {@code LocalDateTime} n'a pas
 * de fuseau, un {@code Instant} n'a pas de calendrier. Il faut donc <b>fournir
 * un fuseau</b> — c'est tout ce que fait cette classe, avec le fuseau métier.</p>
 */
public final class BusinessTime {

    /**
     * Fuseau métier. Les journées sont découpées en heure locale : un comptable
     * qui demande « le 12 » attend les opérations du 12 à Douala, pas de 01h00
     * le 12 à 01h00 le 13 en UTC.
     */
    public static final ZoneId ZONE = ZoneId.of("Africa/Douala");

    private BusinessTime() {
    }

    /** Premier instant du jour — borne basse d'un intervalle. */
    public static Instant startOfDay(LocalDate day) {
        return day.atStartOfDay(ZONE).toInstant();
    }

    /**
     * Dernier instant du jour — borne haute d'un intervalle.
     *
     * <p>⚠️ À utiliser avec les requêtes en {@code BETWEEN}, qui sont
     * <b>inclusives</b> : passer {@code atStartOfDay()} comme borne haute
     * exclurait toute la journée de fin sauf minuit pile.</p>
     */
    public static Instant endOfDay(LocalDate day) {
        return day.atTime(LocalTime.MAX).atZone(ZONE).toInstant();
    }

    /** Date-heure locale reçue d'une API → instant. */
    public static Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZONE).toInstant();
    }

    /** Instant lu en base → date-heure locale, pour affichage. */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZONE);
    }
}
