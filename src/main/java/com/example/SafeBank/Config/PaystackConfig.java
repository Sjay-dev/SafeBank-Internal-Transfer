package com.example.SafeBank.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

@Configuration
@ConditionalOnProperty(prefix = "external-transfer", name = "provider", havingValue = "paystack")
public class PaystackConfig {

    @Value("${paystack.secret-key}")
    private String paystackSecretKey;

    @Bean
    public RestClient paystackRestClient() {
        Assert.hasText(paystackSecretKey, "PAYSTACK_SECRET_KEY must be configured when external-transfer.provider=paystack");
        return RestClient.builder()
                .baseUrl("https://api.paystack.co")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + paystackSecretKey)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
