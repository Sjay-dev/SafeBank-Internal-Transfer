package com.example.SafeBank.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

@Configuration
public class PaystackConfig {

    @Bean
    public RestClient paystackRestClient(@Value("${paystack.secret-key}") String paystackSecretKey) {
        Assert.hasText(paystackSecretKey, "PAYSTACK_SECRET_KEY must be configured");
        return RestClient.builder()
                .baseUrl("https://api.paystack.co")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + paystackSecretKey)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
