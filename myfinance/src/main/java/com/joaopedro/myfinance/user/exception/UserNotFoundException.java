package com.joaopedro.myfinance.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("Usuário não encontrado.");
    }
}
