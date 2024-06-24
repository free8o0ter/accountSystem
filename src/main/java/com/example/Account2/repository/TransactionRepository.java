package com.example.Account2.repository;

import com.example.Account2.domain.Account;
import com.example.Account2.domain.AccountUser;
import com.example.Account2.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

}
