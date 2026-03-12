package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Request.GoogleLoginRequest;
import com.example.SafeBank.DTO.Response.AuthResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;



@SpringBootTest
class AuthServiceTest {

    @MockitoBean
    private GoogleTokenVerifierService googleTokenVerifierService;

    @Autowired
    private AuthService authService;

    @Test
    void testGoogleLogin() {

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("test@gmail.com");
        payload.put("name", "Test User");

        when(googleTokenVerifierService.verify(any()))
                .thenReturn(payload);

        AuthResponse response = authService.googleLogin(
                new GoogleLoginRequest("fake-token")
        );

        Assertions.assertEquals("test@gmail.com", response.email());
    }
}
