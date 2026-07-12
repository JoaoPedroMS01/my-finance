package com.joaopedro.myfinance.transaction.controller;

import com.joaopedro.myfinance.security.CustomUserDetails;
import com.joaopedro.myfinance.transaction.dto.CreateTransactionRequest;
import com.joaopedro.myfinance.transaction.dto.TransactionFilterRequest;
import com.joaopedro.myfinance.transaction.dto.TransactionResponse;
import com.joaopedro.myfinance.transaction.dto.UpdateTransactionRequest;
import com.joaopedro.myfinance.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findAllByUser(@AuthenticationPrincipal CustomUserDetails userDetails, TransactionFilterRequest filter) {
        List<TransactionResponse> transactions = transactionService.findAllByUser(userDetails.getId(), filter);
        return ResponseEntity.ok().body(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @PathVariable Long id) {
        TransactionResponse transaction = transactionService.findById(userDetails.getId(), id);
        return ResponseEntity.ok().body(transaction);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      @RequestBody @Valid CreateTransactionRequest request) {
        TransactionResponse response = transactionService.create(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      @PathVariable Long id,
                                                      @RequestBody @Valid UpdateTransactionRequest request) {
        TransactionResponse response = transactionService.update(userDetails.getId(), id, request);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable Long id) {
        transactionService.delete(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
