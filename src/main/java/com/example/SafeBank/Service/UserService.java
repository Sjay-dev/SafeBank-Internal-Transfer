package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import com.example.SafeBank.DTO.Response.UserResponse;
import com.example.SafeBank.Entities.User;
import com.example.SafeBank.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUserByAccountNumber(String accountNumber) {

        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new CustomExceptions.UserNotFoundException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAccountNumber(),
                user.getBalance(),
                user.getCreatedAt()
        );
    }
}
