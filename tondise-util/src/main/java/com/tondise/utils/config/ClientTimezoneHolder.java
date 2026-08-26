package com.tondise.utils.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.time.ZoneId;

@Component
@RequestScope
public class ClientTimezoneHolder {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Africa/Douala");

    private final ZoneId zoneId;

    public ClientTimezoneHolder(HttpServletRequest request) {
        ZoneId zoneId1;
        String tz = request.getHeader("X-Timezone");
        if (tz != null) {
            try {
                zoneId1 = ZoneId.of(tz);
            } catch (Exception e) {
                zoneId1 = DEFAULT_ZONE;
            }
        } else {
            zoneId1 = DEFAULT_ZONE; // ← header absent → Europe/Paris
        }
        this.zoneId = zoneId1;
    }

    public ZoneId getZoneId() { return zoneId; }
}