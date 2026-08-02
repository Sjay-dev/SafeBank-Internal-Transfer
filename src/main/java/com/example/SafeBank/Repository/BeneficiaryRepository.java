package com.example.SafeBank.Repository;

import com.example.SafeBank.Entities.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    Optional<Beneficiary> findByUser_IdAndAccountNumberAndBankCode(Long userId, String accountNumber, String bankCode);
}
