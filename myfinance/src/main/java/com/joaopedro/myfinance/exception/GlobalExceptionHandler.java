package com.joaopedro.myfinance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_KEY = "error";

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleCategoryAlreadyExists(CategoryAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(ERROR_KEY, ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCategoryNotFound(CategoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
    }

    @ExceptionHandler(CategoryNotBelongsToUserException.class)
    public ResponseEntity<Map<String, String>> handleCategoryNotBelongsToUser(CategoryNotBelongsToUserException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(ERROR_KEY, ex.getMessage()));
    }

    @ExceptionHandler(CategoryNotEditableException.class)
    public ResponseEntity<Map<String, String>> handleCategoryNotEditable(CategoryNotEditableException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(ERROR_KEY, ex.getMessage()));
    }

    @ExceptionHandler(CategoryNotDeletableException.class)
    public ResponseEntity<Map<String, String>> handleCategoryNotDeletable(CategoryNotDeletableException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(ERROR_KEY, ex.getMessage()));
    }
}
