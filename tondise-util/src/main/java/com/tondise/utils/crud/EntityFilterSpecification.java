package com.tondise.utils.crud;

import com.tondise.utils.exception.BadRequestException;
import com.tondise.utils.absrtractServices.AbstractService;
import com.tondise.utils.abstractModel.AbstractEntity;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/**
 * Construction d'un filtre dynamique {@code (colonne, valeur, opérateur)} reçu
 * de l'URL vers une {@link Specification} JPA.
 *
 * <p><b>Pourquoi cette classe existe.</b> Cette logique vivait dans une méthode
 * {@code private} d'{@link AbstractService}.
 * Elle y était impossible à tester isolément et impossible à spécialiser.
 * Ici, c'est un composant autonome — testable sans base de données, et
 * réutilisable en dehors de toute hiérarchie d'héritage.</p>
 *
 * <p>Elle apporte en plus la prise en charge d'{@link Instant}, qui est le type
 * des colonnes d'audit ({@code created}, {@code updated}) de
 * {@link AbstractEntity} depuis leur migration en
 * {@code timestamptz} — un filtre par date sur ces colonnes échouait auparavant.</p>
 *
 * <p>⚠️ <b>Hypothèse assumée</b> : les bornes de journée sont calculées dans le
 * fuseau {@link #BUSINESS_ZONE} (UTC+1). C'est valable pour l'ensemble des
 * produits tondise aujourd'hui déployés. À revoir le jour où un pays sur un autre
 * fuseau est intégré.</p>
 */
@Slf4j
public final class EntityFilterSpecification {

    /**
     * Les bornes de journée sont calculées en heure locale camerounaise : un
     * comptable qui filtre « le 12 » attend les opérations du 12 à Douala,
     * pas de 01h00 le 12 à 01h00 le 13 en UTC.
     */
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Africa/Douala");

    private EntityFilterSpecification() {
    }

    /**
     * @param column   nom de l'attribut d'entité, ou de la colonne SQL
     *                 ({@code id_transaction}, {@code created_at}…)
     * @param value    valeur à comparer ; {@code début|fin} pour {@code between}
     * @param operator contains | startsWith | endsWith | equals | greaterThan |
     *                 lessThan | between
     */
    public static <T> Specification<T> of(String column, String value, String operator) {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("created")));
            if (column == null || value == null) {
                return null;
            }

            String attribute = resolveAttribute(root, column);
            Path<?> path = root.get(attribute);
            Class<?> type = path.getJavaType();

            if ("between".equals(operator)) {
                if (!value.contains("|")) {
                    throw new BadRequestException("Pour 'between', la valeur attendue est 'début|fin'");
                }
                String[] parts = value.split("\\|");
                return betweenPredicate(cb, path, type, parts[0], parts[1]);
            }

            return switch (operator) {
                case "contains" -> containsPredicate(cb, path, type, value);
                case "startsWith" -> {
                    requireString(type, "startsWith");
                    yield cb.like(cb.lower(path.as(String.class)), value.toLowerCase() + "%");
                }
                case "endsWith" -> {
                    requireString(type, "endsWith");
                    yield cb.like(cb.lower(path.as(String.class)), "%" + value.toLowerCase());
                }
                case "equals" -> cb.equal(path, convert(value, type));
                case "greaterThan" -> comparable(cb, path, type, value, true);
                case "lessThan" -> comparable(cb, path, type, value, false);
                default -> throw new BadRequestException("Opérateur invalide : " + operator);
            };
        };
    }

    // ── Opérateurs ───────────────────────────────────────────────────────────

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static jakarta.persistence.criteria.Predicate betweenPredicate(
            jakarta.persistence.criteria.CriteriaBuilder cb, Path<?> path, Class<?> type,
            String startStr, String endStr) {

        if (type == LocalDate.class) {
            return cb.between((Path) path, LocalDate.parse(startStr), LocalDate.parse(endStr));
        }
        if (type == LocalDateTime.class) {
            return cb.between((Path) path,
                    LocalDate.parse(startStr).atStartOfDay(),
                    LocalDate.parse(endStr).atTime(23, 59, 59, 999_999_000));
        }
        // Colonnes d'audit migrées en timestamptz
        if (type == Instant.class) {
            return cb.between((Path) path,
                    LocalDate.parse(startStr).atStartOfDay(BUSINESS_ZONE).toInstant(),
                    LocalDate.parse(endStr).plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant());
        }
        throw new BadRequestException("'between' ne s'applique qu'aux champs de type date");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static jakarta.persistence.criteria.Predicate containsPredicate(
            jakarta.persistence.criteria.CriteriaBuilder cb, Path<?> path, Class<?> type, String value) {

        if (type.isEnum()) {
            return cb.equal(path, convert(value, type));
        }
        if (type == String.class) {
            return cb.like(cb.lower(path.as(String.class)), "%" + value.toLowerCase() + "%");
        }
        // UUID : recherche par fragment, via cast en texte — UUID.fromString
        // refuserait un début d'identifiant
        if (type == UUID.class) {
            return cb.like(cb.lower(path.as(String.class)), "%" + value.toLowerCase() + "%");
        }
        if (type == LocalDate.class) {
            LocalDate day = LocalDate.parse(value);
            return cb.between((Path) path, day, day);
        }
        if (type == LocalDateTime.class) {
            LocalDate day = LocalDate.parse(value);
            return cb.between((Path) path, day.atStartOfDay(), day.atTime(23, 59, 59, 999_999_000));
        }
        if (type == Instant.class) {
            LocalDate day = LocalDate.parse(value);
            return cb.between((Path) path,
                    day.atStartOfDay(BUSINESS_ZONE).toInstant(),
                    day.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant());
        }
        throw new BadRequestException("'contains' ne s'applique qu'aux champs texte, UUID, enum ou date");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static jakarta.persistence.criteria.Predicate comparable(
            jakarta.persistence.criteria.CriteriaBuilder cb, Path<?> path, Class<?> type,
            String value, boolean greater) {

        if (!Comparable.class.isAssignableFrom(type)) {
            throw new BadRequestException("Comparaison non supportée pour le type " + type.getSimpleName());
        }
        Comparable<?> bound = (Comparable<?>) convert(value, type);
        return greater ? cb.greaterThan((Path) path, (Comparable) bound)
                : cb.lessThan((Path) path, (Comparable) bound);
    }

    private static void requireString(Class<?> type, String operator) {
        if (type != String.class) {
            throw new BadRequestException(operator + " ne s'applique qu'aux champs texte");
        }
    }

    // ── Résolution de colonne ────────────────────────────────────────────────

    /**
     * Traduit un nom de colonne SQL vers l'attribut d'entité correspondant :
     * l'API Criteria ne connaît que les attributs. Accepte l'attribut exact,
     * le snake_case ({@code pay_token}), les alias d'audit
     * ({@code created_at} → {@code created}) et les colonnes {@code id_xxx},
     * ramenées à {@code id}. Erreur 400 explicite si rien ne correspond.
     */
    static <T> String resolveAttribute(Root<T> root, String column) {
        try {
            root.get(column);
            return column;
        } catch (IllegalArgumentException ignored) {
            String candidate = column.startsWith("id_") ? "id" : snakeToCamel(column);
            candidate = switch (candidate) {
                case "createdAt" -> "created";
                case "updatedAt" -> "updated";
                default -> candidate;
            };
            try {
                root.get(candidate);
                return candidate;
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Colonne inconnue : '" + column
                        + "'. Utilisez le nom de l'attribut de l'entité (ex. id, status, created).");
            }
        }
    }

    static String snakeToCamel(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        boolean upperNext = false;
        for (char c : value.toCharArray()) {
            if (c == '_') {
                upperNext = true;
            } else {
                sb.append(upperNext ? Character.toUpperCase(c) : c);
                upperNext = false;
            }
        }
        return sb.toString();
    }

    // ── Conversion de valeur ─────────────────────────────────────────────────

    @SuppressWarnings({"unchecked", "rawtypes"})
    static Object convert(String value, Class<?> targetType) {
        if (targetType == String.class) return value;

        if (targetType.isEnum()) {
            try {
                return Enum.valueOf((Class<Enum>) targetType, value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Valeur '" + value + "' invalide pour "
                        + targetType.getSimpleName());
            }
        }
        if (targetType == UUID.class) {
            try {
                return UUID.fromString(value);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Valeur '" + value
                        + "' invalide : 'equals' attend un UUID complet (utilisez 'contains' pour un fragment)");
            }
        }
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(value);
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value);
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(value);
        if (targetType == BigDecimal.class) return new BigDecimal(value);
        if (targetType == LocalDate.class) return LocalDate.parse(value);

        if (targetType == LocalDateTime.class) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException ex) {
                return LocalDate.parse(value).atStartOfDay();
            }
        }
        if (targetType == Instant.class) {
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException ex) {
                return LocalDate.parse(value).atStartOfDay(BUSINESS_ZONE).toInstant();
            }
        }
        throw new BadRequestException("Type non supporté pour le filtrage : " + targetType.getSimpleName());
    }
}
