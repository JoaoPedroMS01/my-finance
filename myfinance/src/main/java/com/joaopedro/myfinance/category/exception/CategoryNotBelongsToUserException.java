package com.joaopedro.myfinance.category.exception;

public class CategoryNotBelongsToUserException extends RuntimeException {
    public CategoryNotBelongsToUserException() {
        super("Categoria não pertence ao usuário");
    }
}
