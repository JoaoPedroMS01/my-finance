package com.joaopedro.myfinance.exception;

import com.joaopedro.myfinance.category.exception.*;
import com.joaopedro.myfinance.transaction.exception.InvalidAmountException;
import com.joaopedro.myfinance.user.exception.EmailAlreadyInUseException;
import com.joaopedro.myfinance.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
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

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyInUse(EmailAlreadyInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(ERROR_KEY, ex.getMessage()));
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAmount(InvalidAmountException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
