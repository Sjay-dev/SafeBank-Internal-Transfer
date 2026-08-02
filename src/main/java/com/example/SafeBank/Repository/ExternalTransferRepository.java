package com.example.SafeBank.Repository;

import com.example.SafeBank.Entities.ExternalTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface ExternalTransferRepository extends JpaRepository<ExternalTransfer, Long> {
    Optional<ExternalTransfer> findByReferenceAndUser_Email(String reference, String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from ExternalTransfer t join fetch t.user where t.reference = :reference and t.user.email = :email")
    Optional<ExternalTransfer> findByReferenceAndUserEmailForUpdate(
            @Param("reference") String reference, @Param("email") String email);
}
