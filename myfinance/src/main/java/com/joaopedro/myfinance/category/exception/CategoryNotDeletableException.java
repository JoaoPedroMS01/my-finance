package com.joaopedro.myfinance.category.exception;

public class CategoryNotDeletableException extends RuntimeException {
    public CategoryNotDeletableException() {
        super("Categoria padrão não pode ser deletada");
    }
}
