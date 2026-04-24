package com.joaopedro.myfinance.service;

import com.joaopedro.myfinance.dto.CategoryResponse;
import com.joaopedro.myfinance.dto.CreateCategoryRequest;
import com.joaopedro.myfinance.entity.Category;
import com.joaopedro.myfinance.entity.User;
import com.joaopedro.myfinance.exception.CategoryAlreadyExistsException;
import com.joaopedro.myfinance.exception.UserNotFoundException;
import com.joaopedro.myfinance.repository.CategoryRepository;
import com.joaopedro.myfinance.repository.UserRepository;
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
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));
        String categoryName = createCategoryRequest.getName().trim();
        String normalizedName = categoryName.toLowerCase();

        if (categoryRepository.existsByNameAndUserIdIsNull(normalizedName)) {
                throw new CategoryAlreadyExistsException("Categoria já existe como padrão.");
        }

        if (categoryRepository.existsByNameAndUserId(normalizedName, userId)) {
            throw new CategoryAlreadyExistsException("Categoria já existe para este usuário.");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);
        return toDto(savedCategory);
    }

    private CategoryResponse toDto(Category category) {
        CategoryResponse dto = new CategoryResponse();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setIsDefault(category.getUser() == null);
        return dto;
    }
}
