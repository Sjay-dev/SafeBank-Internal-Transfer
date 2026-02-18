package com.example.SafeBank.DTO.Request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "Sender account number is required")
        String senderAccountNumber,

        @NotBlank(message = "Receiver account number is required")
        String receiverAccountNumber,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        String description
) {
}
