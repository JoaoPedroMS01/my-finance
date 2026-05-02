package com.joaopedro.myfinance.transaction.exception;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException() {
        super("Valor deve ser maior que zero.");
    }
}
