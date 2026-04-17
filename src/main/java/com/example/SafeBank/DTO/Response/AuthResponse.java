package com.example.SafeBank.DTO.Response;

public record AuthResponse(
        String token,
        String name,
        String email,
        String accountNumber,
        String balance
) {
}
