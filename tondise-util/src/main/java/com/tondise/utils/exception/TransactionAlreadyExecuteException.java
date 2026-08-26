package com.tondise.utils.exception;

public class TransactionAlreadyExecuteException extends RuntimeException {

    public TransactionAlreadyExecuteException(String message) {
        super(message);
    }

    public TransactionAlreadyExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionAlreadyExecuteException(Throwable cause) {
        super(cause);
    }

    public TransactionAlreadyExecuteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public TransactionAlreadyExecuteException() {
    }
}
