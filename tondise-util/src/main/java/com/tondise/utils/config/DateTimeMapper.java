package com.tondise.utils.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class DateTimeMapper {

    private final ClientTimezoneHolder timezoneHolder;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * MapStruct appelle cette méthode automatiquement
     * pour tout champ LocalDateTime → String
     */
    public String toLocalizedString(LocalDateTime dateTime) {
        if (dateTime == null) return null;

        return dateTime
                .atZone(ZoneId.of("UTC"))           // base = UTC
                .withZoneSameInstant(timezoneHolder.getZoneId()) // → timezone client
                .format(FORMATTER);
    }

    /**
     * Pour les Instant (si tu utilises Instant en base)
     */
    public String toLocalizedString(Instant instant) {
        if (instant == null) return null;

        return instant
                .atZone(timezoneHolder.getZoneId())
                .format(FORMATTER);
    }

    /**
     * Retourne le timezone utilisé (utile dans le DTO)
     */
    public String currentTimezone() {
        return timezoneHolder.getZoneId().toString();
    }
}