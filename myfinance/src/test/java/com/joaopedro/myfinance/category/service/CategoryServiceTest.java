package com.joaopedro.myfinance.category.service;

import com.joaopedro.myfinance.category.domain.Category;
import com.joaopedro.myfinance.category.dto.CategoryResponse;
import com.joaopedro.myfinance.category.dto.CreateCategoryRequest;
import com.joaopedro.myfinance.category.exception.*;
import com.joaopedro.myfinance.category.repository.CategoryRepository;
import com.joaopedro.myfinance.user.domain.User;
import com.joaopedro.myfinance.user.exception.UserNotFoundException;
import com.joaopedro.myfinance.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private CreateCategoryRequest buildRequest(String name) {
        var request = new CreateCategoryRequest();
        request.setName(name);
        return request;
    }

    private User buildUser(Long id) {
        var user = new User();
        user.setId(id);
        return user;
    }

    private Category buildCategory(Long id, String name, User user) {
        var category = new Category();
        category.setId(id);
        category.setName(name);
        category.setUser(user);
        return category;
    }

    @Test
    void shouldReturnAllCategoriesForUser() {
        var user = buildUser(1L);
        var userCategory = buildCategory(1L, "Minha Categoria", user);
        var defaultCategory = buildCategory(2L, "Alimentação", null);

        when(categoryRepository.findByUserIdOrUserIdIsNull(1L))
                .thenReturn(Arrays.asList(userCategory, defaultCategory));

        List<CategoryResponse> result = categoryService.findAllByUser(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertFalse(result.get(0).isDefault());
        assertTrue(result.get(1).isDefault());

        verify(categoryRepository).findByUserIdOrUserIdIsNull(1L);
    }

    @Test
    void shouldCreateCategoryWhenDataIsValid() {
        var user = buildUser(1L);
        var request = buildRequest(" Viagem ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull("Viagem")).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCaseAndUserId("Viagem", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        CategoryResponse result = categoryService.create(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Viagem", result.getName());
        assertFalse(result.isDefault());

        verify(userRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundOnCreate() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        var request = buildRequest("Viagem");

        assertThrows(UserNotFoundException.class, () -> categoryService.create(99L, request));

        verify(userRepository).findById(99L);
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExistsAsDefault() {
        var user = buildUser(1L);
        var request = buildRequest("Alimentação");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull("Alimentação")).thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.create(1L, request));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExistsForUser() {
        var user = buildUser(1L);
        var request = buildRequest("Viagem");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull("Viagem")).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCaseAndUserId("Viagem", 1L)).thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.create(1L, request));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCategoryWhenDataIsValid() {
        var user = buildUser(1L);
        var category = buildCategory(1L, "Viagem", user);
        var request = buildRequest(" Férias ");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull("Férias")).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCaseAndIdNotAndUserId("Férias", 1L, 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse result = categoryService.update(1L, 1L, request);

        assertNotNull(result);
        assertEquals("Férias", result.getName());
        assertFalse(result.isDefault());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFoundOnUpdate() {
        var request = buildRequest("Teste");

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.update(1L, 999L, request));

        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingDefaultCategory() {
        var category = buildCategory(1L, "Alimentação", null);
        var request = buildRequest("Novo Nome");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(CategoryNotEditableException.class, () -> categoryService.update(1L, 1L, request));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingCategoryThatDoesNotBelongToUser() {
        var anotherUser = buildUser(2L);
        var category = buildCategory(1L, "Viagem", anotherUser);
        var request = buildRequest("Novo Nome");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(CategoryNotBelongsToUserException.class, () -> categoryService.update(1L, 1L, request));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithNameThatAlreadyExistsAsDefault() {
        var user = buildUser(1L);
        var category = buildCategory(1L, "Viagem", user);
        var request = buildRequest("Alimentação");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull("Alimentação")).thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.update(1L, 1L, request));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithNameThatAlreadyExistsForUser() {
        var user = buildUser(1L);
        var category = buildCategory(1L, "Viagem", user);
        var request = buildRequest("Férias");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull("Férias")).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCaseAndIdNotAndUserId("Férias", 1L, 1L)).thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.update(1L, 1L, request));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldDeleteCategoryWhenItExistsAndBelongsToUser() {
        var user = buildUser(1L);
        var category = buildCategory(1L, "Viagem", user);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(category);

        assertDoesNotThrow(() -> categoryService.delete(1L, 1L));

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).delete(category);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCategory() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.delete(1L, 999L));

        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingDefaultCategory() {
        var category = buildCategory(1L, "Alimentação", null);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(CategoryNotDeletableException.class, () -> categoryService.delete(1L, 1L));

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingCategoryThatDoesNotBelongToUser() {
        var anotherUser = buildUser(2L);
        var category = buildCategory(1L, "Viagem", anotherUser);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(CategoryNotBelongsToUserException.class, () -> categoryService.delete(1L, 1L));

        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
