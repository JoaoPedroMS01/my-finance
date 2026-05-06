package com.joaopedro.myfinance.transaction.service;

import com.joaopedro.myfinance.category.domain.Category;
import com.joaopedro.myfinance.category.exception.CategoryNotBelongsToUserException;
import com.joaopedro.myfinance.category.exception.CategoryNotFoundException;
import com.joaopedro.myfinance.category.repository.CategoryRepository;
import com.joaopedro.myfinance.transaction.domain.Transaction;
import com.joaopedro.myfinance.transaction.domain.TransactionType;
import com.joaopedro.myfinance.transaction.dto.CreateTransactionRequest;
import com.joaopedro.myfinance.transaction.exception.InvalidAmountException;
import com.joaopedro.myfinance.transaction.repository.TransactionRepository;
import com.joaopedro.myfinance.user.domain.User;
import com.joaopedro.myfinance.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private CreateTransactionRequest buildRequest(LocalDate date, String description, BigDecimal amount, TransactionType type, Long categoryId) {
        var request = new CreateTransactionRequest();
        request.setDate(date);
        request.setDescription(description);
        request.setAmount(amount);
        request.setType(type);
        request.setCategoryId(categoryId);
        return request;
    }

    @Test
    void shouldCreateTransactionWhenDataIsValid() {
        var request = buildRequest(
                LocalDate.of(2026, 1, 15),
                " Salário ",
                BigDecimal.valueOf(20000.00),
                TransactionType.INCOME,
                1L
        );

        var category = new Category();
        category.setId(1L);
        category.setName("Salário");
        category.setUser(null); // global category

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction t = invocation.getArgument(0);
                    t.setId(1L);
                    return t;
                });

        var result = transactionService.create(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(request.getDate(), result.getDate());
        assertEquals(request.getDescription().trim(), result.getDescription());
        assertEquals(request.getAmount(), result.getAmount());
        assertEquals(TransactionType.INCOME, result.getType());
        assertEquals(request.getCategoryId(), result.getCategoryId());
        assertEquals("Salário", result.getCategoryName());

        verify(categoryRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsInvalid() {
        var request = buildRequest(LocalDate.now(), "Teste", BigDecimal.valueOf(-100), TransactionType.EXPENSE, 1L);

        assertThrows(InvalidAmountException.class, () -> transactionService.create(1L, request));

        verifyNoInteractions(categoryRepository, transactionRepository);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        var request = buildRequest(LocalDate.now(), "Teste", BigDecimal.valueOf(100), TransactionType.EXPENSE, 999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> transactionService.create(1L, request));

        verify(categoryRepository).findById(999L);
        verifyNoMoreInteractions(categoryRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotBelongToUser() {
        var request = buildRequest(LocalDate.now(), "Teste", BigDecimal.valueOf(100), TransactionType.EXPENSE, 1L);

        var anotherUser = new User();
        anotherUser.setId(2L);

        var category = new Category();
        category.setId(1L);
        category.setName("Categoria Teste");
        category.setUser(anotherUser); // category of another user

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(CategoryNotBelongsToUserException.class, () -> transactionService.create(1L, request));

        verify(categoryRepository).findById(1L);
        verifyNoMoreInteractions(categoryRepository);
        verifyNoInteractions(transactionRepository);
    }

}
