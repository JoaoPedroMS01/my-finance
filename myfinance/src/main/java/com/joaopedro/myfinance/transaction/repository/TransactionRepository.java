package com.joaopedro.myfinance.transaction.repository;

import com.joaopedro.myfinance.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> findByUserId(Long userId);
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
}
