package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Response.BankResponse;
import com.example.SafeBank.DTO.Response.PaystackAccountResolution;
import com.example.SafeBank.DTO.Response.PaystackApiResponse;
import com.example.SafeBank.DTO.Response.PaystackRecipientResponse;
import com.example.SafeBank.DTO.Response.PaystackTransferResponse;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "external-transfer", name = "provider", havingValue = "paystack")
public class PaystackService implements ExternalBankTransferGateway {

    private final RestClient restClient;

    public PaystackService(RestClient paystackRestClient) {
        this.restClient = paystackRestClient;
    }

    public PaystackApiResponse<List<BankResponse>> getAllBanks() {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/bank").queryParam("country", "nigeria").build())
                .retrieve().body(new ParameterizedTypeReference<PaystackApiResponse<List<BankResponse>>>() {}));
    }

    public PaystackAccountResolution resolveAccount(String accountNumber, String bankCode) {
        PaystackApiResponse<PaystackAccountResolution> response = execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/bank/resolve")
                        .queryParam("account_number", accountNumber)
                        .queryParam("bank_code", bankCode).build())
                .retrieve().body(new ParameterizedTypeReference<PaystackApiResponse<PaystackAccountResolution>>() {}));
        return successfulData(response, "Unable to resolve bank account");
    }

    public PaystackRecipientResponse createRecipient(String name, String accountNumber, String bankCode) {
        PaystackApiResponse<PaystackRecipientResponse> response = execute(() -> restClient.post()
                .uri("/transferrecipient")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Map.of("type", "nuban", "name", name, "account_number", accountNumber,
                        "bank_code", bankCode, "currency", "NGN"))
                .retrieve().body(new ParameterizedTypeReference<PaystackApiResponse<PaystackRecipientResponse>>() {}));
        return successfulData(response, "Unable to create transfer recipient");
    }

    public PaystackTransferResponse initiateTransfer(long amountInKobo, String recipientCode,
                                                     String reference, String narration) {
        PaystackApiResponse<PaystackTransferResponse> response = execute(() -> restClient.post()
                .uri("/transfer")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Map.of("source", "balance", "amount", amountInKobo, "recipient", recipientCode,
                        "reference", reference, "reason", narration, "currency", "NGN"))
                .retrieve().body(new ParameterizedTypeReference<PaystackApiResponse<PaystackTransferResponse>>() {}));
        return successfulData(response, "Unable to initiate transfer");
    }

    public PaystackTransferResponse verifyTransfer(String reference) {
        PaystackApiResponse<PaystackTransferResponse> response = execute(() -> restClient.get()
                .uri("/transfer/verify/{reference}", reference)
                .retrieve().body(new ParameterizedTypeReference<PaystackApiResponse<PaystackTransferResponse>>() {}));
        return successfulData(response, "Unable to verify transfer");
    }

    private <T> T successfulData(PaystackApiResponse<T> response, String fallbackMessage) {
        if (response == null || !response.isStatus() || response.getData() == null) {
            throw new CustomExceptions.PaystackException(
                    response != null && response.getMessage() != null ? response.getMessage() : fallbackMessage);
        }
        return response.getData();
    }

    private <T> T execute(PaystackCall<T> call) {
        try {
            return call.execute();
        } catch (RestClientException ex) {
            throw new CustomExceptions.PaystackException("Paystack request failed");
        }
    }

    @FunctionalInterface
    private interface PaystackCall<T> {
        T execute();
    }
}
