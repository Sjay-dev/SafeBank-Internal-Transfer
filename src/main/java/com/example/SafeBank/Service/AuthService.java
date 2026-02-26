package com.example.SafeBank.Service;

import com.example.SafeBank.Security.JwtTokenProvider;
import com.example.SafeBank.DTO.Request.AuthRequest;
import com.example.SafeBank.DTO.Request.UserRequest;
import com.example.SafeBank.DTO.Response.AuthResponse;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import com.example.SafeBank.Entities.User;
import com.example.SafeBank.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(UserRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .build();

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                "Bearer",
                user.getEmail(),
                user.getAccountNumber().toString()
        );
    }

    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new CustomExceptions.AuthenticationFailedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomExceptions.AuthenticationFailedException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                "Bearer",
                user.getEmail(),
                user.getAccountNumber().toString()
        );
    }

    private Long generateAccountNumber() {
        // Generates a 10-digit random number
        return ThreadLocalRandom.current().nextLong(1000000000L, 9999999999L);
    }



}
