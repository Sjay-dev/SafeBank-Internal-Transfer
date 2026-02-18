package com.example.SafeBank.Controllers;

import com.example.SafeBank.DTO.Response.UserResponse;
import com.example.SafeBank.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{accountNumber}")
    public UserResponse getUser(
            @PathVariable String accountNumber) {

        return userService.getUserByAccountNumber(accountNumber);
    }
}
