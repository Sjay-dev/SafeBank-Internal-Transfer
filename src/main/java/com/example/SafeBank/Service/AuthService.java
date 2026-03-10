package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Request.GoogleLoginRequest;
import com.example.SafeBank.Security.JwtTokenProvider;
import com.example.SafeBank.DTO.Request.AuthRequest;
import com.example.SafeBank.DTO.Request.UserRequest;
import com.example.SafeBank.DTO.Response.AuthResponse;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import com.example.SafeBank.Entities.User;
import com.example.SafeBank.Repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
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
    private final GoogleTokenVerifierService googleTokenVerifierService; // Add this

    // Existing register
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
                user.getName(),
                user.getEmail(),
                user.getAccountNumber().toString()
        );
    }

    // Existing login
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
                user.getName(),
                user.getEmail(),
                user.getAccountNumber().toString()
        );
    }

    // --------------------------
    // NEW: Google login method
    // --------------------------
    public AuthResponse googleLogin(GoogleLoginRequest request) {

        // Verify Google ID token
        GoogleIdToken.Payload payload = googleTokenVerifierService.verify(request.getIdToken());

        if (payload == null) {
            throw new RuntimeException("Invalid Google token");
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");

        // Find existing user or create new one
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .name(name)
                            .email(email)
                            .password("") // No password for Google users
                            .accountNumber(generateAccountNumber())
                            .balance(BigDecimal.ZERO)
                            .build();
                    return userRepository.save(newUser);
                });

        // Generate JWT
        String token = jwtTokenProvider.generateToken(user.getEmail());

        // Return response matching existing format
        return new AuthResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getAccountNumber().toString()
        );
    }

    // --------------------------
    // Private helper
    // --------------------------
    private Long generateAccountNumber() {
        return ThreadLocalRandom.current().nextLong(1000000000L, 9999999999L);
    }
}
