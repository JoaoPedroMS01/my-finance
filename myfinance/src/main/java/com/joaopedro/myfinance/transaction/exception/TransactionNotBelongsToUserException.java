package com.joaopedro.myfinance.transaction.exception;

public class TransactionNotBelongsToUserException extends RuntimeException {
    public TransactionNotBelongsToUserException() {
        super("Transação não pertence ao usuário");
    }
}
