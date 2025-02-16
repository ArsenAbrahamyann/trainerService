package org.example.trainer.exeption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;


/**
 * Centralized exception handler for handling exceptions globally in the application.
 * Provides consistent error response formatting for various exception types.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    /**
     * Builds the error response based on the exception details and request information.
     *
     * @param message The error message.
     * @param status  The HTTP status of the error.
     * @return Response entity containing the formatted error response.
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(message, status);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handles the ResponseStatusException when the response status is set to a specific status code,
     * typically indicating invalid credentials or other authorization issues.
     * Logs the warning and returns a standardized error response with a 401 Unauthorized status.
     *
     * @param ex the exception thrown due to invalid status or authorization issue
     * @return a ResponseEntity containing the error details and status code
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("Invalid credentials: {}", ex.getMessage());
        return buildErrorResponse("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }

    /**
       * Handles exceptions arising from failures during workload updates.
     */
    @ExceptionHandler(WorkloadException.class)
    public ResponseEntity<ErrorResponse> handleWorkloadUpdateException(WorkloadException ex) {
        log.error("Workload not found - Status: {}, Details: {}", ex.getHttpStatus(), ex.getMessage());
        return buildErrorResponse("workload  not found: " + ex.getMessage(), ex.getHttpStatus());
    }

    /**
     * Handles general application-level exceptions, providing a consistent response format.
     *
     * @param e The {@link ApplicationException} thrown within the application.
     * @return A {@link ResponseEntity} with a standardized {@link ErrorResponse}.
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException e) {
        log.error("An Application error occurred: {}", e.getMessage(), e);
        return buildErrorResponse("An Application error occurred", HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles general, unexpected exceptions, providing a fallback error response.
     *
     * @param e The {@link Exception} that occurred unexpectedly.
     * @return A {@link ResponseEntity} with a standardized {@link ErrorResponse}.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("An unexpected error occurred: {}", e.getMessage(), e);
        return buildErrorResponse("An unexpected error occurred", HttpStatus.BAD_REQUEST);
    }


}
