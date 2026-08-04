package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Response.BankResponse;
import com.example.SafeBank.DTO.Response.PaystackAccountResolution;
import com.example.SafeBank.DTO.Response.PaystackApiResponse;
import com.example.SafeBank.DTO.Response.PaystackRecipientResponse;
import com.example.SafeBank.DTO.Response.PaystackTransferResponse;

import java.util.List;

/**
 * Port used by the external-transfer use case. Implementations may call Paystack
 * or emulate the same provider responses locally.
 */
public interface ExternalBankTransferGateway {
    PaystackApiResponse<List<BankResponse>> getAllBanks();
    PaystackAccountResolution resolveAccount(String accountNumber, String bankCode);
    PaystackRecipientResponse createRecipient(String name, String accountNumber, String bankCode);
    PaystackTransferResponse initiateTransfer(long amountInKobo, String recipientCode, String reference, String narration);
    PaystackTransferResponse verifyTransfer(String reference);
}
