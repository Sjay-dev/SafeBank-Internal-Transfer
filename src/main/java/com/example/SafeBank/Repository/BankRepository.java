package com.example.SafeBank.Repository;

import com.example.SafeBank.Entities.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, String> {
    List<Bank> findAllByOrderByNameAsc();
    Optional<Bank> findTopByOrderByLastRefreshedAtDesc();
}
