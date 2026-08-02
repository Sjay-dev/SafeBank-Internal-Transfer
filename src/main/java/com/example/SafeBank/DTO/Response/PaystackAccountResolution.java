package com.example.SafeBank.DTO.Response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaystackAccountResolution(
        @JsonProperty("account_number") String accountNumber,
        @JsonProperty("account_name") String accountName
) {
}
