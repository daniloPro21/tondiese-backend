package com.tondise.utils.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public interface WithAuthenticationSupport {
    default CurrentUser getCurrentUser() {
        Authentication authentication = Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .orElseThrow();
        return CurrentUser.builder()
                .authentication(authentication)
                .build();
    }
}
