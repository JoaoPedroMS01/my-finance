package com.joaopedro.myfinance.transaction.service;

import com.joaopedro.myfinance.category.domain.Category;
import com.joaopedro.myfinance.category.exception.CategoryNotBelongsToUserException;
import com.joaopedro.myfinance.category.exception.CategoryNotFoundException;
import com.joaopedro.myfinance.category.repository.CategoryRepository;
import com.joaopedro.myfinance.transaction.domain.Transaction;
import com.joaopedro.myfinance.transaction.dto.CreateTransactionRequest;
import com.joaopedro.myfinance.transaction.dto.TransactionResponse;
import com.joaopedro.myfinance.transaction.dto.UpdateTransactionRequest;
import com.joaopedro.myfinance.transaction.exception.InvalidAmountException;
import com.joaopedro.myfinance.transaction.exception.TransactionNotFoundException;
import com.joaopedro.myfinance.transaction.repository.TransactionRepository;
import com.joaopedro.myfinance.user.domain.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    public TransactionResponse create(Long userId, CreateTransactionRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }

        User user = new User();
        user.setId(userId);

        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(CategoryNotFoundException::new);

        if (isCategoryNotValidForUser(category, user)) {
            throw new CategoryNotBelongsToUserException();
        }

        Transaction transaction = new Transaction();
        transaction.setDate(request.getDate());
        transaction.setDescription(request.getDescription().trim());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(category);
        transaction.setUser(user);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return toDto(savedTransaction);
    }

    public List<TransactionResponse> findAllByUser(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream()
                .map(this::toDto).toList();
    }

    public TransactionResponse findById(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(TransactionNotFoundException::new);

        return toDto(transaction);
    }

    public TransactionResponse update(Long userId, Long transactionId, UpdateTransactionRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }

        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(TransactionNotFoundException::new);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(CategoryNotFoundException::new);

        User user = new User();
        user.setId(userId);

        if (isCategoryNotValidForUser(category, user)) {
            throw new CategoryNotBelongsToUserException();
        }

        transaction.setDate(request.getDate());
        transaction.setDescription(request.getDescription().trim());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(category);

        Transaction updatedTransaction = transactionRepository.save(transaction);

        return toDto(updatedTransaction);
    }

    public void delete(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(TransactionNotFoundException::new);

        transactionRepository.delete(transaction);
    }

    private boolean isCategoryNotValidForUser(Category category, User user) {
        return category.getUser() != null && !category.getUser().getId().equals(user.getId());
    }

    private TransactionResponse toDto(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setDate(transaction.getDate());
        response.setDescription(transaction.getDescription());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setCategoryId(transaction.getCategory().getId());
        response.setCategoryName(transaction.getCategory().getName());
        return response;
    }
}
