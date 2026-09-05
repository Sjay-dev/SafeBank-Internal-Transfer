package com.example.SafeBank.Service;

import com.example.SafeBank.Config.DemoBalanceProperties;
import com.example.SafeBank.DTO.Request.AuthRequest;
import com.example.SafeBank.DTO.Request.GoogleLoginRequest;
import com.example.SafeBank.DTO.Request.UserRequest;
import com.example.SafeBank.DTO.Response.AuthResponse;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import com.example.SafeBank.Entities.User;
import com.example.SafeBank.Repository.UserRepository;
import com.example.SafeBank.Security.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final DemoBalanceProperties demoBalanceProperties;

    @Transactional
    public AuthResponse register(UserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = createNewUser(request.name(), request.email(), request.password());
        return authResponseFor(user);
    }

    /**
     * Used by the startup seeder. Existing users are left completely unchanged,
     * so a restart can never reset their balance or credentials.
     */
    @Transactional
    public boolean createUserIfMissing(String name, String email, String rawPassword) {
        if (userRepository.findByEmail(email).isPresent()) {
            return false;
        }

        createNewUser(name, email, rawPassword);
        return true;
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new CustomExceptions.AuthenticationFailedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomExceptions.AuthenticationFailedException("Invalid credentials");
        }

        return authResponseFor(user);
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = googleTokenVerifierService.verify(request.getIdToken());
        if (payload == null) {
            throw new RuntimeException("Invalid Google token");
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(name, email, ""));

        return authResponseFor(user);
    }

    private User createNewUser(String name, String email, String rawPassword) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .accountNumber(generateAccountNumber().toString())
                .balance(demoBalanceProperties.getStartingBalance())
                .build());
    }

    private AuthResponse authResponseFor(User user) {
        return new AuthResponse(
                jwtTokenProvider.generateToken(user.getEmail()),
                user.getName(),
                user.getEmail(),
                user.getAccountNumber(),
                user.getBalance().toString()
        );
    }

    private Long generateAccountNumber() {
        for (int attempt = 0; attempt < 10; attempt++) {
            long accountNumber = ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L);
            if (!userRepository.existsByAccountNumber(Long.toString(accountNumber))) {
                return accountNumber;
            }
        }
        throw new IllegalStateException("Unable to generate a unique account number");
    }
}
