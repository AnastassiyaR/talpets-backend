package com.backend.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // Resource already exists
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // Unauthorized access
    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password", request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ErrorResponse> handleEmptyCart(EmptyCartException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCardException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCard(InvalidCardException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(PhotoStorageException.class)
    public ResponseEntity<ErrorResponse> handlePhotoStorage(PhotoStorageException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    // Validation errors (with @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        // Iterate over all validation errors and map each field to its corresponding error message
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });


        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", errors);
        response.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    // Handles cases when a query or path parameter has an invalid type
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String paramName = ex.getName();
        Object value = ex.getValue();
        Class<?> requiredType = ex.getRequiredType();

        String message;

        if (requiredType != null && requiredType.isEnum()) {
            String validValues = String.join(", ",
                    java.util.Arrays.stream(requiredType.getEnumConstants())
                            .map(Object::toString)
                            .toArray(String[]::new)
            );
            message = String.format("Invalid value '%s' for parameter '%s'. Valid values: %s",
                    value, paramName, validValues);
        } else {
            message = String.format("Invalid value '%s' for parameter '%s'", value, paramName);
        }

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // Generic exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    // Helper method to reduce duplication
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }
}
