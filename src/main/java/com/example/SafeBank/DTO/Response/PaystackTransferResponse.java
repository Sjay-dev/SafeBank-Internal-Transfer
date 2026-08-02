package com.example.SafeBank.DTO.Response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaystackTransferResponse(
        String reference,
        String status,
        @JsonProperty("transfer_code") String transferCode
) {
}
