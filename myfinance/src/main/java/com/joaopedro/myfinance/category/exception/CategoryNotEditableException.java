package com.joaopedro.myfinance.category.exception;

public class CategoryNotEditableException extends RuntimeException {
    public CategoryNotEditableException() {
        super("Categoria padrão não pode ser editada");
    }
}
