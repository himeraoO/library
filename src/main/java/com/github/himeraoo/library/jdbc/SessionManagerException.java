package com.github.himeraoo.library.jdbc;

public class SessionManagerException extends RuntimeException{
    public SessionManagerException(String message) {
        super(message);
    }

    public SessionManagerException(Throwable cause) {
        super(cause);
    }

    public SessionManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionManagerException() {
    }

    public SessionManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
