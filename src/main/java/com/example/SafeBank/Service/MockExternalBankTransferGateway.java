package com.example.SafeBank.Service;

import com.example.SafeBank.Config.ExternalTransferProviderProperties;
import com.example.SafeBank.DTO.Response.BankResponse;
import com.example.SafeBank.DTO.Response.PaystackAccountResolution;
import com.example.SafeBank.DTO.Response.PaystackApiResponse;
import com.example.SafeBank.DTO.Response.PaystackRecipientResponse;
import com.example.SafeBank.DTO.Response.PaystackTransferResponse;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Local deterministic substitute for Paystack's transfer APIs. */
@Service
@ConditionalOnProperty(prefix = "external-transfer", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockExternalBankTransferGateway implements ExternalBankTransferGateway {
    private final ExternalTransferProviderProperties properties;
    private final Map<String, Recipient> recipients = new ConcurrentHashMap<>();
    private final Map<String, PaystackTransferResponse> transfers = new ConcurrentHashMap<>();

    public MockExternalBankTransferGateway(ExternalTransferProviderProperties properties) {
        this.properties = properties;
    }

    @Override
    public PaystackApiResponse<java.util.List<BankResponse>> getAllBanks() {
        PaystackApiResponse<List<BankResponse>> response = new PaystackApiResponse<>();
        response.setStatus(true);
        response.setMessage("Mock bank list");
        response.setData(List.of(bank("058", "Guaranty Trust Bank"), bank("044", "Access Bank")));
        return response;
    }

    @Override
    public PaystackAccountResolution resolveAccount(String accountNumber, String bankCode) {
        validateAccount(accountNumber, bankCode);
        return new PaystackAccountResolution(accountNumber, "Mock Account " + accountNumber.substring(6));
    }

    @Override
    public PaystackRecipientResponse createRecipient(String name, String accountNumber, String bankCode) {
        validateAccount(accountNumber, bankCode);
        String code = "RCP_MOCK_" + UUID.randomUUID().toString().replace("-", "");
        recipients.put(code, new Recipient(accountNumber));
        return new PaystackRecipientResponse(code);
    }

    @Override
    public PaystackTransferResponse initiateTransfer(long amountInKobo, String recipientCode, String reference, String narration) {
        if (amountInKobo <= 0) throw new CustomExceptions.PaystackException("Transfer amount must be positive");
        Recipient recipient = recipients.get(recipientCode);
        if (recipient == null) throw new CustomExceptions.PaystackException("Unknown mock recipient");
        String status = properties.getMock().getFailedAccountNumbers().contains(recipient.accountNumber()) ? "failed" : "success";
        PaystackTransferResponse result = new PaystackTransferResponse(reference, status, "TRF_MOCK_" + UUID.randomUUID().toString().replace("-", ""));
        transfers.put(reference, result);
        return result;
    }

    @Override
    public PaystackTransferResponse verifyTransfer(String reference) {
        PaystackTransferResponse result = transfers.get(reference);
        if (result == null) throw new CustomExceptions.PaystackException("Mock transfer not found");
        return result;
    }

    private void validateAccount(String accountNumber, String bankCode) {
        if (accountNumber == null || !accountNumber.matches("\\d{10}") || bankCode == null || bankCode.isBlank()) {
            throw new CustomExceptions.PaystackException("Invalid account details");
        }
    }

    private BankResponse bank(String code, String name) {
        BankResponse bank = new BankResponse();
        bank.setCode(code);
        bank.setName(name);
        return bank;
    }

    private record Recipient(String accountNumber) { }
}
