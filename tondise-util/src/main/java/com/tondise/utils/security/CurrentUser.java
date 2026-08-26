package com.tondise.utils.security;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Builder
@RequiredArgsConstructor
public class CurrentUser {
    @NonNull
    private final Authentication authentication;

    public boolean hasAuthority(String authority) {
        return Objects.nonNull(authority) &&
                authentication
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(Objects::nonNull)
                        .distinct()
                        .anyMatch(authority::equals);
    }

    public String getUsername() {
        var principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof Jwt) {
            return ((Jwt) principal).getClaimAsString(StandardClaimNames.PREFERRED_USERNAME);
        }
        throw new UnsupportedOperationException(
                String.format("Operation not supported for type '%s'", authentication.getClass())
        );
    }

    public boolean hasAnyAuthority(String... scopes) {
        return Optional.ofNullable(scopes)
                .stream()
                .flatMap(Stream::of)
                .anyMatch(this::hasAuthority);
    }
}
