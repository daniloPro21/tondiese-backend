package com.tondise.ecommerce.config.security;

import com.tondise.ecommerce.config.properties.CorsProperties;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Ressource server OAuth2 adossé à Keycloak.
 *
 * <h2>Écart assumé par rapport à task-force-remita</h2>
 * Remita utilise {@code KeycloakResolver} (tondise-util) + le
 * {@link JwtAuthenticationConverter} par défaut de Spring, qui lit le claim
 * {@code scope} et produit des autorités {@code SCOPE_xxx} — adapté à leur
 * modèle de ~150 permissions fines par entité.
 *
 * <p>Tondise n'a que deux profils (client, administrateur back-office) : on
 * utilise donc directement {@code issuer-uri} + un
 * {@link JwtAuthenticationConverter} personnalisé qui lit
 * {@code realm_access.roles} (rôles realm Keycloak {@code USER}/{@code ADMIN})
 * et les expose comme autorités Spring {@code ROLE_xxx}. {@code KeycloakResolver}
 * n'est pas importé ici : il fige la conversion sur le claim {@code scope}, ce
 * qui n'est pas le modèle retenu pour ce projet. Le reste de tondise-util
 * (Keycloak admin API via {@code KeycloakService}, stockage MinIO, gestion
 * d'erreurs) reste utilisé tel quel.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsProperties corsProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityRoutes.WHITE_LIST_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityRoutes.PUBLIC_CATALOG_GET).permitAll()
                        .requestMatchers(SecurityRoutes.ADMIN_PREFIX).hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    /**
     * Lit les rôles realm Keycloak ({@code realm_access.roles}, ex. {@code ["USER"]})
     * et les convertit en autorités Spring {@code ROLE_USER}/{@code ROLE_ADMIN}.
     * Conserve en plus les autorités {@code SCOPE_xxx} par défaut (claim {@code scope}),
     * au cas où un client scope standard serait ajouté plus tard.
     */
    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

        Converter<Jwt, Collection<GrantedAuthority>> realmRolesConverter = jwt -> {
            Object realmAccess = jwt.getClaims().get("realm_access");
            if (!(realmAccess instanceof java.util.Map<?, ?> map)) {
                return List.of();
            }
            Object roles = map.get("roles");
            if (!(roles instanceof Collection<?> roleCollection)) {
                return List.of();
            }
            return roleCollection.stream()
                    .map(Object::toString)
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        };

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> Stream.concat(
                        scopeConverter.convert(jwt).stream(),
                        realmRolesConverter.convert(jwt).stream())
                .collect(Collectors.toList()));
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
