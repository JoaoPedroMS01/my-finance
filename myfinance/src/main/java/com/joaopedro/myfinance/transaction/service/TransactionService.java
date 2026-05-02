package com.joaopedro.myfinance.transaction.service;

import com.joaopedro.myfinance.category.domain.Category;
import com.joaopedro.myfinance.category.exception.CategoryNotBelongsToUserException;
import com.joaopedro.myfinance.category.exception.CategoryNotFoundException;
import com.joaopedro.myfinance.category.repository.CategoryRepository;
import com.joaopedro.myfinance.transaction.domain.Transaction;
import com.joaopedro.myfinance.transaction.dto.CreateTransactionRequest;
import com.joaopedro.myfinance.transaction.dto.TransactionResponse;
import com.joaopedro.myfinance.transaction.exception.InvalidAmountException;
import com.joaopedro.myfinance.transaction.repository.TransactionRepository;
import com.joaopedro.myfinance.user.domain.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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

    private boolean isCategoryNotValidForUser(Category category, User user) {
        return category.getUser() != null && !category.getUser().getId().equals(user.getId());
    }

    private TransactionResponse toDto(Transaction savedTransaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(savedTransaction.getId());
        response.setDate(savedTransaction.getDate());
        response.setDescription(savedTransaction.getDescription());
        response.setAmount(savedTransaction.getAmount());
        response.setType(savedTransaction.getType());
        response.setCategoryId(savedTransaction.getCategory().getId());
        response.setCategoryName(savedTransaction.getCategory().getName());
        return response;
    }
}
