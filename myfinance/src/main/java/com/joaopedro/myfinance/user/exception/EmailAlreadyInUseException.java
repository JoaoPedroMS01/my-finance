package com.joaopedro.myfinance.user.exception;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException() {
        super("E-mail já cadastrado.");
    }
}
