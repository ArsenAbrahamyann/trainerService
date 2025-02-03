package org.example.trainer.exeption;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Represents a general application exception that captures an HTTP status and error message.
 * Provides logging functionality for error messages.
 */
@Getter
@Slf4j
public class ApplicationException extends RuntimeException {
    private final HttpStatus httpStatus;

    /**
     * Constructs a new ApplicationException with the specified message and HTTP status.
     *
     * @param message   The detail message for the exception.
     * @param httpStatus The HTTP status associated with the exception.
     */
    protected ApplicationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR;
        logError(message, this.httpStatus);
    }

    /**
     * Logs the error message and HTTP status.
     *
     * @param message   The detail message for the exception.
     * @param httpStatus The HTTP status associated with the exception.
     */
    private void logError(String message, HttpStatus httpStatus) {
        log.error("{} - HTTP Status: {}", message, httpStatus);
    }
}
