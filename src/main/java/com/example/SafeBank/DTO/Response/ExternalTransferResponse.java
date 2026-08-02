package com.example.SafeBank.DTO.Response;

import com.example.SafeBank.Entities.Enum.TransferStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record ExternalTransferResponse(
        String reference,
        String transferCode,
        TransferStatus status,
        BigDecimal amount,
        String bankCode,
        String accountNumber,
        String accountName,
        String narration,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
