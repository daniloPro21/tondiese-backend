package com.tondise.utils.exception;

public class KeycloakUserCreationFailedException extends RuntimeException {

    public KeycloakUserCreationFailedException(String message) {
        super(message);
    }
    public KeycloakUserCreationFailedException() {

    }
}
