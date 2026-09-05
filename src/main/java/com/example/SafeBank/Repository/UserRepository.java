package com.example.SafeBank.Repository;

import com.example.SafeBank.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAccountNumber(String accountNumber);
    Optional<User> findByEmail(String email);
    boolean existsByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.email = :email")
    Optional<User> findByEmailForUpdate(@Param("email") String email);
}
