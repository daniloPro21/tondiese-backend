package com.tondise.utils.exception;

public class UniqueConstraintException extends RuntimeException {
    public UniqueConstraintException(String message) {
        super(message);
    }
    public UniqueConstraintException(String message, Throwable cause) {
        super(message, cause);
    }
}
