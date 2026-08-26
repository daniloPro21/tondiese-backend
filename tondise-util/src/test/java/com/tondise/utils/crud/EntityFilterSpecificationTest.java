package com.tondise.utils.crud;

import com.tondise.utils.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ces tests s'exécutent sans contexte Spring ni base de données — c'est
 * précisément ce que la version enfermée dans une méthode {@code private}
 * d'{@code AbstractService} interdisait.
 */
class EntityFilterSpecificationTest {

    private static final ZoneId DOUALA = ZoneId.of("Africa/Douala");

    /** Enum local : le test ne doit dépendre d'aucun projet consommateur. */
    private enum Statut {SUCCESS, FAILED, PENDING}

    @Test
    @DisplayName("snake_case et alias d'audit sont traduits en attributs d'entité")
    void snakeToCamel_traduitLesNomsDeColonnes() {
        assertEquals("payToken", EntityFilterSpecification.snakeToCamel("pay_token"));
        assertEquals("phoneReceiver", EntityFilterSpecification.snakeToCamel("phone_receiver"));
        assertEquals("id", EntityFilterSpecification.snakeToCamel("id"));
    }

    @Test
    @DisplayName("Un montant est converti sans perte de précision")
    void convert_bigDecimal_conservePrecision() {
        // Le passage par Double (implémentation héritée) donnait 101.49999999999999
        assertEquals(new BigDecimal("101.50"),
                EntityFilterSpecification.convert("101.50", BigDecimal.class));
        assertEquals(new BigDecimal("0.1"),
                EntityFilterSpecification.convert("0.1", BigDecimal.class));
    }

    @Test
    @DisplayName("Un enum est reconnu quelle que soit la casse")
    void convert_enum_insensibleALaCasse() {
        assertEquals(Statut.SUCCESS, EntityFilterSpecification.convert("success", Statut.class));
        assertEquals(Statut.SUCCESS, EntityFilterSpecification.convert("SUCCESS", Statut.class));
    }

    @Test
    @DisplayName("Un enum inconnu produit une 400, pas une 500")
    void convert_enumInvalide_leveBadRequest() {
        assertThrows(BadRequestException.class,
                () -> EntityFilterSpecification.convert("PAS_UN_STATUT", Statut.class));
    }

    @Test
    @DisplayName("Un UUID incomplet produit une 400 explicite")
    void convert_uuidIncomplet_leveBadRequest() {
        UUID complet = UUID.randomUUID();
        assertEquals(complet, EntityFilterSpecification.convert(complet.toString(), UUID.class));
        assertThrows(BadRequestException.class,
                () -> EntityFilterSpecification.convert("064b569a", UUID.class));
    }

    @Test
    @DisplayName("Une date seule est interprétée en heure de Douala pour un Instant")
    void convert_instant_utiliseLeFuseauMetier() {
        // Colonnes d'audit migrées en timestamptz : non géré par la version héritée
        Instant attendu = LocalDate.of(2026, 7, 1).atStartOfDay(DOUALA).toInstant();
        assertEquals(attendu, EntityFilterSpecification.convert("2026-07-01", Instant.class));

        Instant iso = Instant.parse("2026-07-01T10:15:30Z");
        assertEquals(iso, EntityFilterSpecification.convert("2026-07-01T10:15:30Z", Instant.class));
    }

    @Test
    @DisplayName("Une date seule est acceptée là où un LocalDateTime est attendu")
    void convert_localDateTime_accepteLesDeuxFormats() {
        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0),
                EntityFilterSpecification.convert("2026-07-01", LocalDateTime.class));
        assertEquals(LocalDateTime.of(2026, 7, 1, 8, 30),
                EntityFilterSpecification.convert("2026-07-01T08:30:00", LocalDateTime.class));
    }

    @Test
    @DisplayName("Un type non filtrable produit une 400, pas une 500")
    void convert_typeNonSupporte_leveBadRequest() {
        assertThrows(BadRequestException.class,
                () -> EntityFilterSpecification.convert("x", Object.class));
    }
}
