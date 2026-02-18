package com.example.SafeBank.Repository;

import com.example.SafeBank.Entities.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findBySenderIdOrReceiverId(Long senderId, Long receiverId);
}
