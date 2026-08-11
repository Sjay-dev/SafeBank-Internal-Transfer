package com.example.SafeBank.Service;

/**
 * Executes the local stand-in for Paystack money movement. Paystack is still
 * used directly for bank, account-resolution, and recipient APIs.
 */
public interface ExternalBankTransferGateway {
    MockTransferResult executeSuccessfulTransfer();
}
