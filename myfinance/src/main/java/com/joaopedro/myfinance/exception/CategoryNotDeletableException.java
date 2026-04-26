package com.joaopedro.myfinance.exception;

public class CategoryNotDeletableException extends RuntimeException {
    public CategoryNotDeletableException() {
        super("Categoria padrão não pode ser deletada");
    }
}
