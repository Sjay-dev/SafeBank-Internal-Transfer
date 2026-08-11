package com.example.SafeBank.Repository;

import com.example.SafeBank.Entities.ExternalTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExternalTransferRepository extends JpaRepository<ExternalTransfer, Long> {
    Optional<ExternalTransfer> findByReferenceAndUser_Email(String reference, String email);
}
