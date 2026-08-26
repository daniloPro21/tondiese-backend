package com.tondise.utils.exception;

public class ForeignKeyConstraintException extends RuntimeException {
    public ForeignKeyConstraintException(String message) {
        super(message);
    }
    public ForeignKeyConstraintException(String message, Throwable cause) {
        super(message, cause);
    }
}
