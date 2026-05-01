package com.joaopedro.myfinance.category.repository;

import com.joaopedro.myfinance.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdOrUserIdIsNull(Long userId);

    boolean existsByNameIgnoreCaseAndUserIdIsNull(String name);

    boolean existsByNameIgnoreCaseAndUserId(String categoryName, Long userId);

    boolean existsByNameIgnoreCaseAndIdNotAndUserId(String name, Long id, Long userId);

}
