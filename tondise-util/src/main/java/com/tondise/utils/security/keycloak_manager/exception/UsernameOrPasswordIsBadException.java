package com.tondise.utils.security.keycloak_manager.exception;

public class UsernameOrPasswordIsBadException extends RuntimeException {
    public UsernameOrPasswordIsBadException() {
        super("invalid Username or Password");
    }
}
