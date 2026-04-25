package com.joaopedro.myfinance.exception;

public class CategoryNotEditableException extends RuntimeException {
    public CategoryNotEditableException() {
        super("Categoria padrão não pode ser editada");
    }
}
