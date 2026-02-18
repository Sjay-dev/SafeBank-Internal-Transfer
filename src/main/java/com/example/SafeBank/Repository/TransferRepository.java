package com.example.SafeBank.Repository;

import com.example.SafeBank.Entities.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Page<Transfer> findBySender_IdOrReceiver_Id(
            Long senderId,
            Long receiverId,
            Pageable pageable
    );


}
