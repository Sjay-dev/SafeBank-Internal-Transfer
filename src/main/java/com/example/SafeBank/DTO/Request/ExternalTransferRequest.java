package com.example.SafeBank.DTO.Request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ExternalTransferRequest(
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 15, fraction = 2) BigDecimal amount,
        @NotBlank @Size(max = 20) String bankCode,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "Account number must contain exactly 10 digits") String accountNumber,
        @NotBlank @Size(max = 500) String narration
) {
}
