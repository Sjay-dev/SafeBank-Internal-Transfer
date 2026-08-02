package com.example.SafeBank.DTO.Response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaystackRecipientResponse(@JsonProperty("recipient_code") String recipientCode) {
}
