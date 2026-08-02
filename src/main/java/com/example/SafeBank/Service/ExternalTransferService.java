package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Request.ExternalTransferRequest;
import com.example.SafeBank.DTO.Response.*;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import com.example.SafeBank.Entities.Beneficiary;
import com.example.SafeBank.Entities.Enum.TransferStatus;
import com.example.SafeBank.Entities.ExternalTransfer;
import com.example.SafeBank.Entities.User;
import com.example.SafeBank.Repository.BeneficiaryRepository;
import com.example.SafeBank.Repository.ExternalTransferRepository;
import com.example.SafeBank.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExternalTransferService {

    private final UserRepository userRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ExternalTransferRepository externalTransferRepository;
    private final PaystackService paystackService;

    public AccountResolutionResponse resolveAccount(String accountNumber, String bankCode) {
        PaystackAccountResolution resolved = paystackService.resolveAccount(accountNumber, bankCode);
        return new AccountResolutionResponse(resolved.accountName(), resolved.accountNumber(), bankCode);
    }

    @Transactional
    public ExternalTransferResponse createTransfer(String email, ExternalTransferRequest request) {
        User user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found"));
        if (user.getBalance().compareTo(request.amount()) < 0) {
            throw new CustomExceptions.InsufficientBalanceException("Insufficient balance");
        }

        PaystackAccountResolution resolution = paystackService.resolveAccount(request.accountNumber(), request.bankCode());
        Beneficiary beneficiary = findOrCreateBeneficiary(user, request, resolution.accountName());
        String reference = "safebank_" + UUID.randomUUID();

        user.setBalance(user.getBalance().subtract(request.amount()));
        PaystackTransferResponse paystackTransfer = paystackService.initiateTransfer(
                toKobo(request.amount()), beneficiary.getRecipientCode(), reference, request.narration());
        TransferStatus status = toStatus(paystackTransfer.status());
        if (status == TransferStatus.FAILED) {
            throw new CustomExceptions.TransferFailedException("Paystack rejected the transfer");
        }

        ExternalTransfer transfer = ExternalTransfer.builder()
                .user(user).amount(request.amount()).bankCode(request.bankCode())
                .accountNumber(request.accountNumber()).accountName(resolution.accountName())
                .recipientCode(beneficiary.getRecipientCode()).reference(reference)
                .transferCode(paystackTransfer.transferCode()).narration(request.narration()).status(status).build();
        return toResponse(externalTransferRepository.save(transfer));
    }

    @Transactional
    public ExternalTransferResponse verifyTransfer(String email, String reference) {
        ExternalTransfer transfer = externalTransferRepository.findByReferenceAndUserEmailForUpdate(reference, email)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Transfer not found"));
        PaystackTransferResponse result = paystackService.verifyTransfer(reference);
        TransferStatus previousStatus = transfer.getStatus();
        TransferStatus updatedStatus = toStatus(result.status());
        transfer.setStatus(updatedStatus);
        if (result.transferCode() != null) {
            transfer.setTransferCode(result.transferCode());
        }
        if (updatedStatus == TransferStatus.FAILED && previousStatus != TransferStatus.FAILED) {
            User user = userRepository.findByEmailForUpdate(email)
                    .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found"));
            user.setBalance(user.getBalance().add(transfer.getAmount()));
        }
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

    private long toKobo(BigDecimal amount) {
        try {
            return amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException ex) {
            throw new CustomExceptions.InvalidTransferException("Amount must be expressed in kobo precision");
        }
    }

    private TransferStatus toStatus(String status) {
        if (status == null) return TransferStatus.PENDING;
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "success" -> TransferStatus.SUCCESS;
            case "failed", "reversed" -> TransferStatus.FAILED;
            default -> TransferStatus.PENDING;
        };
    }

    private ExternalTransferResponse toResponse(ExternalTransfer transfer) {
        return new ExternalTransferResponse(transfer.getReference(), transfer.getTransferCode(), transfer.getStatus(),
                transfer.getAmount(), transfer.getBankCode(), transfer.getAccountNumber(), transfer.getAccountName(),
                transfer.getNarration(), transfer.getCreatedAt(), transfer.getUpdatedAt());
    }
}
