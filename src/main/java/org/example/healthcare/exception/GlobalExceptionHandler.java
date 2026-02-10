package org.example.healthcare.exception;

import org.example.healthcare.dto.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(MessageResponse.builder().message(ex.getMessage()).build());
    }

    // 409 - Duplicate resource (username, email, etc.)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<MessageResponse> handleDuplicateResource(DuplicateResourceException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(MessageResponse.builder().message(ex.getMessage()).build());
    }

    // 409 - Double booking
    @ExceptionHandler(DoubleBookingException.class)
    public ResponseEntity<MessageResponse> handleDoubleBooking(DoubleBookingException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(MessageResponse.builder().message(ex.getMessage()).build());
    }

    // 401 - Bad credentials (wrong username/password)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<MessageResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponse.builder().message("Invalid username or password").build());
    }

    // 400 - Validation errors (@NotBlank, @Email, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    // 500 - Everything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MessageResponse.builder().message("An error occurred: " + ex.getMessage()).build());
    }
}