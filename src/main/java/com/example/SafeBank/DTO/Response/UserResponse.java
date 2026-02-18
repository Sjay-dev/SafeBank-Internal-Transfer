package com.example.SafeBank.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserResponse(
        Long userId,
        String name,
        String email,
        String accountNumber,
        BigDecimal balance,
        LocalDateTime createdAt
) {
}
