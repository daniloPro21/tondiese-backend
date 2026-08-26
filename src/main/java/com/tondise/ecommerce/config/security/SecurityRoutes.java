package com.tondise.ecommerce.config.security;

/**
 * Préfixes de route utilisés par {@link SecurityConfig} pour l'autorisation.
 *
 * <p>Inspiré de la classe {@code Patch} de task-force-remita, mais simplifié :
 * Tondise n'a que deux profils (client, admin), pas de scopes fins par entité
 * comme la fintech — les routes admin sont protégées par le rôle realm
 * Keycloak {@code ADMIN} plutôt que par des dizaines de scopes {@code SCOPE_*}.</p>
 */
public class SecurityRoutes {

    public static final String[] WHITE_LIST_URL = {
            "/auth/login",
            "/auth/register",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/payments/webhook/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health/**",
            "/error"
    };

    public static final String[] PUBLIC_CATALOG_GET = {
            "/categories/**",
            "/products/**"
    };

    public static final String ADMIN_PREFIX = "/admin/**";
}
