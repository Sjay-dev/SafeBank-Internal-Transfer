package com.example.SafeBank.DTO.Response;

import com.example.SafeBank.Entities.Enum.TransactionType;
import com.example.SafeBank.Entities.Enum.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        Long transferId,
        String senderAccountNumber,
        String receiverAccountNumber,
        BigDecimal amount,
        String description,
        TransferStatus status,
        LocalDateTime createdAt,
        TransactionType transactionType

) {
}
