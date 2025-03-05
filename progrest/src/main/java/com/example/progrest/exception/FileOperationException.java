package com.example.progrest.exception;

/**
 * Custom exception for file operation failures.
 */
public class FileOperationException extends RuntimeException {

    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
