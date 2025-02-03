package org.example.trainer.exeption;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user is not found in the system.
 */
public class WorkloadException extends ApplicationException {

    /**
     * Constructs a new WorkloadException with the specified message.
     *
     * @param message The detail message.
     */
    public WorkloadException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
