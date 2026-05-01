package com.joaopedro.myfinance.category.service;

import com.joaopedro.myfinance.category.repository.CategoryRepository;
import com.joaopedro.myfinance.category.domain.Category;
import com.joaopedro.myfinance.category.dto.CategoryResponse;
import com.joaopedro.myfinance.category.dto.CreateCategoryRequest;
import com.joaopedro.myfinance.category.exception.*;
import com.joaopedro.myfinance.user.domain.User;
import com.joaopedro.myfinance.user.exception.UserNotFoundException;
import com.joaopedro.myfinance.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<CategoryResponse> findAllByUser(Long userId) {
        return categoryRepository.findByUserIdOrUserIdIsNull(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public CategoryResponse create(Long userId, CreateCategoryRequest createCategoryRequest) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        String categoryName = createCategoryRequest.getName().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull(categoryName)) {
                throw new CategoryAlreadyExistsException("Categoria já existe como padrão.");
        }

        if (categoryRepository.existsByNameIgnoreCaseAndUserId(categoryName, userId)) {
            throw new CategoryAlreadyExistsException("Categoria já existe para este usuário.");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);
        return toDto(savedCategory);
    }

    public CategoryResponse update(Long userId, Long categoryId, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
        String categoryName = request.getName().trim();

        if (category.getUser() == null) {
            throw new CategoryNotEditableException();
        }

        if (!category.getUser().getId().equals(userId)) {
            throw new CategoryNotBelongsToUserException();
        }

        if (categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull(categoryName)) {
            throw new CategoryAlreadyExistsException("Categoria já existe como padrão.");
        }

        if (categoryRepository.existsByNameIgnoreCaseAndIdNotAndUserId(categoryName, categoryId, userId)) {
            throw new CategoryAlreadyExistsException("Categoria já existe para este usuário.");
        }

        category.setName(categoryName);
        Category updatedCategory = categoryRepository.save(category);
        return toDto(updatedCategory);
    }

    public void delete(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        if (category.getUser() == null) {
            throw new CategoryNotDeletableException();
        }

        if (!category.getUser().getId().equals(userId)) {
            throw new CategoryNotBelongsToUserException();
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse toDto(Category category) {
        CategoryResponse dto = new CategoryResponse();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setIsDefault(category.getUser() == null);
        return dto;
    }
}
