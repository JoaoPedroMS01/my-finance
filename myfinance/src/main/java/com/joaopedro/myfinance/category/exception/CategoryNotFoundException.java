package com.joaopedro.myfinance.category.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException() {
        super("Categoria não encontrada");
    }
}
