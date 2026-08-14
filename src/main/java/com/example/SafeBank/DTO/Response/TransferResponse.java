package com.example.SafeBank.DTO.Response;

import com.example.SafeBank.Entities.Enum.TransactionType;
import com.example.SafeBank.Entities.Enum.TransferStatus;
import com.example.SafeBank.Entities.Enum.TransferChannel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        Long transferId,
        String senderAccountNumber,
        String receiverAccountNumber,
        String senderName,
        String receiverName,
        BigDecimal amount,
        String description,
        TransferStatus status,
        LocalDateTime createdAt,
        TransactionType transactionType,
        TransferChannel transferChannel,
        String recipientBankName,
        String recipientBankCode,
        String transferReference,
        String transferCode

) {
}
