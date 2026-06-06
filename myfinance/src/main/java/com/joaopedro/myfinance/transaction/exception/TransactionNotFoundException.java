package com.joaopedro.myfinance.transaction.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException() {
        super("Transação não encontrada");
    }
}
