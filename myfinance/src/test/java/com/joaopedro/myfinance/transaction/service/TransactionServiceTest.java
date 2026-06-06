package com.joaopedro.myfinance.transaction.service;

import com.joaopedro.myfinance.category.domain.Category;
import com.joaopedro.myfinance.category.exception.CategoryNotBelongsToUserException;
import com.joaopedro.myfinance.category.exception.CategoryNotFoundException;
import com.joaopedro.myfinance.category.repository.CategoryRepository;
import com.joaopedro.myfinance.transaction.domain.Transaction;
import com.joaopedro.myfinance.transaction.domain.TransactionType;
import com.joaopedro.myfinance.transaction.dto.CreateTransactionRequest;
import com.joaopedro.myfinance.transaction.dto.TransactionResponse;
import com.joaopedro.myfinance.transaction.dto.UpdateTransactionRequest;
import com.joaopedro.myfinance.transaction.exception.InvalidAmountException;
import com.joaopedro.myfinance.transaction.exception.TransactionNotFoundException;
import com.joaopedro.myfinance.transaction.repository.TransactionRepository;
import com.joaopedro.myfinance.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

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

    // Tests for findAllByUser
    @Test
    void shouldReturnAllTransactionsForUser() {
        var user = new User();
        user.setId(1L);

        var category = new Category();
        category.setId(1L);
        category.setName("Alimentação");

        var transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setDate(LocalDate.of(2026, 1, 15));
        transaction1.setDescription("Mercado");
        transaction1.setAmount(BigDecimal.valueOf(150.00));
        transaction1.setType(TransactionType.EXPENSE);
        transaction1.setCategory(category);
        transaction1.setUser(user);

        var transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setDate(LocalDate.of(2026, 1, 20));
        transaction2.setDescription("Restaurante");
        transaction2.setAmount(BigDecimal.valueOf(80.00));
        transaction2.setType(TransactionType.EXPENSE);
        transaction2.setCategory(category);
        transaction2.setUser(user);

        when(transactionRepository.findByUserId(1L)).thenReturn(Arrays.asList(transaction1, transaction2));

        List<TransactionResponse> result = transactionService.findAllByUser(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Mercado", result.get(0).getDescription());
        assertEquals("Restaurante", result.get(1).getDescription());

        verify(transactionRepository).findByUserId(1L);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoTransactions() {
        when(transactionRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        List<TransactionResponse> result = transactionService.findAllByUser(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(transactionRepository).findByUserId(1L);
    }

    // Tests for findById
    @Test
    void shouldReturnTransactionWhenIdExistsAndBelongsToUser() {
        var user = new User();
        user.setId(1L);

        var category = new Category();
        category.setId(1L);
        category.setName("Salário");

        var transaction = new Transaction();
        transaction.setId(1L);
        transaction.setDate(LocalDate.of(2026, 1, 15));
        transaction.setDescription("Pagamento");
        transaction.setAmount(BigDecimal.valueOf(5000.00));
        transaction.setType(TransactionType.INCOME);
        transaction.setCategory(category);
        transaction.setUser(user);

        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(transaction));

        TransactionResponse result = transactionService.findById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Pagamento", result.getDescription());
        assertEquals(BigDecimal.valueOf(5000.00), result.getAmount());

        verify(transactionRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        when(transactionRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> transactionService.findById(1L, 999L));

        verify(transactionRepository).findByIdAndUserId(999L, 1L);
    }

    // Tests for update
    @Test
    void shouldUpdateTransactionWhenDataIsValid() {
        var user = new User();
        user.setId(1L);

        var oldCategory = new Category();
        oldCategory.setId(1L);
        oldCategory.setName("Alimentação");

        var newCategory = new Category();
        newCategory.setId(2L);
        newCategory.setName("Transporte");
        newCategory.setUser(null); // global category

        var transaction = new Transaction();
        transaction.setId(1L);
        transaction.setDate(LocalDate.of(2026, 1, 15));
        transaction.setDescription("Mercado");
        transaction.setAmount(BigDecimal.valueOf(150.00));
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategory(oldCategory);
        transaction.setUser(user);

        var updateRequest = new UpdateTransactionRequest();
        updateRequest.setDate(LocalDate.of(2026, 1, 20));
        updateRequest.setDescription("Uber");
        updateRequest.setAmount(BigDecimal.valueOf(50.00));
        updateRequest.setType(TransactionType.EXPENSE);
        updateRequest.setCategoryId(2L);

        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(transaction));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse result = transactionService.update(1L, 1L, updateRequest);

        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 1, 20), result.getDate());
        assertEquals("Uber", result.getDescription());
        assertEquals(BigDecimal.valueOf(50.00), result.getAmount());
        assertEquals(2L, result.getCategoryId());

        verify(transactionRepository).findByIdAndUserId(1L, 1L);
        verify(categoryRepository).findById(2L);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithInvalidAmount() {
        var updateRequest = new UpdateTransactionRequest();
        updateRequest.setDate(LocalDate.now());
        updateRequest.setDescription("Teste");
        updateRequest.setAmount(BigDecimal.valueOf(-100));
        updateRequest.setType(TransactionType.EXPENSE);
        updateRequest.setCategoryId(1L);

        assertThrows(InvalidAmountException.class, () -> transactionService.update(1L, 1L, updateRequest));

        verifyNoInteractions(transactionRepository, categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTransaction() {
        var updateRequest = new UpdateTransactionRequest();
        updateRequest.setDate(LocalDate.now());
        updateRequest.setDescription("Teste");
        updateRequest.setAmount(BigDecimal.valueOf(100));
        updateRequest.setType(TransactionType.EXPENSE);
        updateRequest.setCategoryId(1L);

        when(transactionRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> transactionService.update(1L, 999L, updateRequest));

        verify(transactionRepository).findByIdAndUserId(999L, 1L);
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithCategoryThatDoesNotExist() {
        var user = new User();
        user.setId(1L);

        var category = new Category();
        category.setId(1L);
        category.setName("Alimentação");

        var transaction = new Transaction();
        transaction.setId(1L);
        transaction.setDate(LocalDate.of(2026, 1, 15));
        transaction.setDescription("Mercado");
        transaction.setAmount(BigDecimal.valueOf(150.00));
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategory(category);
        transaction.setUser(user);

        var updateRequest = new UpdateTransactionRequest();
        updateRequest.setDate(LocalDate.now());
        updateRequest.setDescription("Teste");
        updateRequest.setAmount(BigDecimal.valueOf(100));
        updateRequest.setType(TransactionType.EXPENSE);
        updateRequest.setCategoryId(999L);

        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(transaction));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> transactionService.update(1L, 1L, updateRequest));

        verify(transactionRepository).findByIdAndUserId(1L, 1L);
        verify(categoryRepository).findById(999L);
    }

    // Tests for delete
    @Test
    void shouldDeleteTransactionWhenItExistsAndBelongsToUser() {
        var user = new User();
        user.setId(1L);

        var category = new Category();
        category.setId(1L);
        category.setName("Alimentação");

        var transaction = new Transaction();
        transaction.setId(1L);
        transaction.setDate(LocalDate.of(2026, 1, 15));
        transaction.setDescription("Mercado");
        transaction.setAmount(BigDecimal.valueOf(150.00));
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategory(category);
        transaction.setUser(user);

        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(transaction));
        doNothing().when(transactionRepository).delete(transaction);

        assertDoesNotThrow(() -> transactionService.delete(1L, 1L));

        verify(transactionRepository).findByIdAndUserId(1L, 1L);
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTransaction() {
        when(transactionRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> transactionService.delete(1L, 999L));

        verify(transactionRepository).findByIdAndUserId(999L, 1L);
        verify(transactionRepository, never()).delete(any(Transaction.class));
    }

}
