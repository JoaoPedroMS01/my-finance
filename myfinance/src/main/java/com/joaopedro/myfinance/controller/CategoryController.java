package com.joaopedro.myfinance.controller;

import com.joaopedro.myfinance.dto.CategoryResponse;
import com.joaopedro.myfinance.dto.CreateCategoryRequest;
import com.joaopedro.myfinance.security.CustomUserDetails;
import com.joaopedro.myfinance.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAllByUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok().body(categoryService.findAllByUser(userDetails.getId()));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid CreateCategoryRequest request) {
        CategoryResponse response = categoryService.create(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("{id}")
    public ResponseEntity<CategoryResponse> update(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @PathVariable Long id,
                                                   @RequestBody @Valid CreateCategoryRequest request) {
        CategoryResponse response = categoryService.update(userDetails.getId(), id, request);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable Long id) {
        categoryService.delete(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
