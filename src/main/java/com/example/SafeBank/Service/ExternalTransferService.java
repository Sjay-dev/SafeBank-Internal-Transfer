package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Request.ExternalTransferRequest;
import com.example.SafeBank.DTO.Response.AccountResolutionResponse;
import com.example.SafeBank.DTO.Response.ExternalTransferResponse;
import com.example.SafeBank.DTO.Response.PaystackAccountResolution;
import com.example.SafeBank.DTO.Response.PaystackRecipientResponse;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import com.example.SafeBank.Entities.Beneficiary;
import com.example.SafeBank.Entities.ExternalTransfer;
import com.example.SafeBank.Entities.User;
import com.example.SafeBank.Repository.BeneficiaryRepository;
import com.example.SafeBank.Repository.ExternalTransferRepository;
import com.example.SafeBank.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalTransferService {

    private final UserRepository userRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ExternalTransferRepository externalTransferRepository;
    private final ExternalBankTransferGateway externalBankTransferGateway;
    private final BankCatalogService bankCatalogService;
    private final PaystackService paystackService;

    public AccountResolutionResponse resolveAccount(String accountNumber, String bankCode) {
        bankCatalogService.requireKnownBankCode(bankCode);
        PaystackAccountResolution resolved = paystackService.resolveAccount(accountNumber, bankCode);
        return new AccountResolutionResponse(resolved.accountName(), resolved.accountNumber(), bankCode);
    }

    @Transactional
    public ExternalTransferResponse createTransfer(String email, ExternalTransferRequest request) {
        bankCatalogService.requireKnownBankCode(request.bankCode());
        User user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found"));
        if (user.getBalance().compareTo(request.amount()) < 0) {
            throw new CustomExceptions.InsufficientBalanceException("Insufficient balance");
        }

        PaystackAccountResolution resolution = paystackService.resolveAccount(request.accountNumber(), request.bankCode());
        Beneficiary beneficiary = findOrCreateBeneficiary(user, request, resolution.accountName());
        MockTransferResult transferResult = externalBankTransferGateway.executeSuccessfulTransfer();

        user.setBalance(user.getBalance().subtract(request.amount()));

        ExternalTransfer transfer = ExternalTransfer.builder()
                .user(user).amount(request.amount()).bankCode(request.bankCode())
                .accountNumber(request.accountNumber()).accountName(resolution.accountName())
                .recipientCode(beneficiary.getRecipientCode()).reference(transferResult.reference())
                .transferCode(transferResult.transferCode()).narration(request.narration())
                .status(transferResult.status()).build();
        return toResponse(externalTransferRepository.save(transfer));
    }

    public ExternalTransferResponse verifyTransfer(String email, String reference) {
        ExternalTransfer transfer = externalTransferRepository.findByReferenceAndUser_Email(reference, email)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Transfer not found"));
        return toResponse(transfer);
    }

    private Beneficiary findOrCreateBeneficiary(User user, ExternalTransferRequest request, String accountName) {
        return beneficiaryRepository.findByUser_IdAndAccountNumberAndBankCode(
                        user.getId(), request.accountNumber(), request.bankCode())
                .orElseGet(() -> {
                    PaystackRecipientResponse recipient = paystackService.createRecipient(
                            accountName, request.accountNumber(), request.bankCode());
                    if (recipient.recipientCode() == null || recipient.recipientCode().isBlank()) {
                        throw new CustomExceptions.RecipientCreationException("Paystack did not return a recipient code");
                    }
                    return beneficiaryRepository.save(Beneficiary.builder().user(user)
                            .accountNumber(request.accountNumber()).bankCode(request.bankCode())
                            .accountName(accountName).recipientCode(recipient.recipientCode()).build());
                });
    }

    private ExternalTransferResponse toResponse(ExternalTransfer transfer) {
        return new ExternalTransferResponse(transfer.getReference(), transfer.getTransferCode(), transfer.getStatus(),
                transfer.getAmount(), transfer.getBankCode(), transfer.getAccountNumber(), transfer.getAccountName(),
                transfer.getNarration(), transfer.getCreatedAt(), transfer.getUpdatedAt());
    }
}
