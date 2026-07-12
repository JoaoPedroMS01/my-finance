package com.joaopedro.myfinance.transaction.specification;

import com.joaopedro.myfinance.transaction.domain.Transaction;
import com.joaopedro.myfinance.transaction.domain.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class TransactionSpecification {

    public static Specification<Transaction> belongsToUser(Long userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Transaction> hasCategory(Long categoryId) {
        if (categoryId == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        if (type == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<Transaction> descriptionContains(String description) {
        if (description == null || description.isBlank()) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("description")),
                        "%" + description.toUpperCase() + "%");
    }

    public static Specification<Transaction> betweenDates(LocalDate start, LocalDate end) {
        return (root, query, criteriaBuilder) -> {
            if (start != null && end != null) {
                return criteriaBuilder.between(root.get("date"), start, end);
            }

            if (start != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("date"), start);
            }

            if (end != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("date"), end);
            }

            return null;
        };
    }

}
