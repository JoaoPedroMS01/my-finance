package com.joaopedro.myfinance.exception;

public class CategoryNotBelongsToUserException extends RuntimeException {
    public CategoryNotBelongsToUserException() {
        super("Categoria não pertence ao usuário");
    }
}
