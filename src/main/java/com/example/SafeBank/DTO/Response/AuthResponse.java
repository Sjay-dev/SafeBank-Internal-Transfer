package com.example.SafeBank.DTO.Response;

public record AuthResponse(
        String token,        // JWT token
        String tokenType,    // usually "Bearer"
        String email,
        String accountNumber
) {
}
