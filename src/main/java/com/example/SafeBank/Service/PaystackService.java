package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Response.BankResponse;
import com.example.SafeBank.DTO.Response.PaystackAccountResolution;
import com.example.SafeBank.DTO.Response.PaystackApiResponse;
import com.example.SafeBank.DTO.Response.PaystackRecipientResponse;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class PaystackService {

    private static final String TEST_BANK_CODE = "001";
    private static final String TEST_MODE_RESOLVE_LIMIT_MESSAGE =
            "Test mode daily limit of 3 live bank resolves exceeded. Use test bank codes 001 or upgrade to live mode.";

    private final RestClient restClient;

    public PaystackService(RestClient paystackRestClient) {
        this.restClient = paystackRestClient;
    }

    public List<BankResponse> getAllBanks() {
        PaystackApiResponse<List<BankResponse>> response = execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/bank")
                        .queryParam("country", "nigeria")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<PaystackApiResponse<List<BankResponse>>>() {}));
        return successfulData(response, "Unable to fetch Nigerian bank list");
    }

    public PaystackAccountResolution resolveAccount(String accountNumber, String bankCode) {
        return resolveAccount(accountNumber, bankCode, false);
    }

    private PaystackAccountResolution resolveAccount(String accountNumber, String bankCode, boolean fallbackAttempted) {
        PaystackApiResponse<PaystackAccountResolution> response;
        try {
            response = resolveAccountWithBankCode(accountNumber, bankCode);
        } catch (RestClientResponseException ex) {
            // Retry only Paystack's documented test-mode rate-limit response with test bank code 001.
            if (!fallbackAttempted && isTestModeResolveLimit(ex)) {
                return resolveAccount(accountNumber, TEST_BANK_CODE, true);
            }
            throw new CustomExceptions.PaystackException("Paystack request failed");
        } catch (RestClientException ex) {
            throw new CustomExceptions.PaystackException("Paystack request failed");
        }

        // Paystack permits test-bank code 001 after its daily live-bank resolution limit is reached.
        if (!fallbackAttempted && isTestModeResolveLimit(response)) {
            return resolveAccount(accountNumber, TEST_BANK_CODE, true);
        }
        return successfulData(response, "Unable to resolve bank account");
    }

    private PaystackApiResponse<PaystackAccountResolution> resolveAccountWithBankCode(
            String accountNumber, String bankCode) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/bank/resolve")
                        .queryParam("account_number", accountNumber)
                        .queryParam("bank_code", bankCode).build())
                .retrieve().body(new ParameterizedTypeReference<PaystackApiResponse<PaystackAccountResolution>>() {});
    }

    private boolean isTestModeResolveLimit(PaystackApiResponse<?> response) {
        return response != null
                && !response.isStatus()
                && response.getMessage() != null
                && response.getMessage().contains(TEST_MODE_RESOLVE_LIMIT_MESSAGE);
    }

    private boolean isTestModeResolveLimit(RestClientResponseException ex) {
        return ex.getStatusCode().value() == 429
                && ex.getResponseBodyAsString().contains(TEST_MODE_RESOLVE_LIMIT_MESSAGE);
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
