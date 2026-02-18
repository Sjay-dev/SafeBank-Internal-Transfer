package com.example.SafeBank.Repository;

import com.example.SafeBank.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAccountNumber(String accountNumber);
    Optional<User> findByEmail(String email);
}
