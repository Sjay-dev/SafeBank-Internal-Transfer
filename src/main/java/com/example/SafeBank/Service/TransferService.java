package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Request.TransferRequest;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import com.example.SafeBank.DTO.Response.TransferResponse;
import com.example.SafeBank.Entities.Enum.TransactionType;
import com.example.SafeBank.Entities.Enum.TransferChannel;
import com.example.SafeBank.Entities.Enum.TransferStatus;
import com.example.SafeBank.Entities.ExternalTransfer;
import com.example.SafeBank.Entities.Transfer;
import com.example.SafeBank.Entities.User;
import com.example.SafeBank.Repository.TransferRepository;
import com.example.SafeBank.Repository.ExternalTransferRepository;
import com.example.SafeBank.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final UserRepository userRepository;
    private final TransferRepository transferRepository;
    private final ExternalTransferRepository externalTransferRepository;

    @Transactional
    public TransferResponse performTransfer(String email, TransferRequest request) {
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Sender not found"));
        User receiver = userRepository.findByAccountNumber(request.receiverAccountNumber())
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Receiver not found"));

        if (sender.getAccountNumber().equals(receiver.getAccountNumber())) {
            throw new CustomExceptions.InvalidTransferException("Cannot transfer to yourself");
        }
        if (sender.getBalance().compareTo(request.amount()) < 0) {
            throw new CustomExceptions.InsufficientBalanceException("Insufficient balance");
        }
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomExceptions.InvalidTransferException("Invalid amount");
        }

        sender.setBalance(sender.getBalance().subtract(request.amount()));
        receiver.setBalance(receiver.getBalance().add(request.amount()));
        userRepository.save(sender);
        userRepository.save(receiver);

        Transfer savedTransfer = transferRepository.save(Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(request.amount())
                .description(request.description())
                .status(TransferStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build());

        return new TransferResponse(
                savedTransfer.getId(),
                sender.getAccountNumber(),
                receiver.getAccountNumber(),
                sender.getName(),
                receiver.getName(),
                savedTransfer.getAmount(),
                savedTransfer.getDescription(),
                savedTransfer.getStatus(),
                savedTransfer.getCreatedAt(),
                TransactionType.DEBIT,
                TransferChannel.SAFE_BANK_TRANSFER,
                null, null, null, null
        );
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getMyTransactionHistory(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found"));

        Pageable requestedPage = PageRequest.of(page, size, Sort.by("createdAt").descending());
        int recordsNeeded = Math.addExact(Math.multiplyExact(page, size), size);
        Pageable sourcePage = PageRequest.of(0, recordsNeeded, Sort.by("createdAt").descending());

        Page<Transfer> safeBankTransfers = transferRepository
                .findBySender_IdOrReceiver_Id(user.getId(), user.getId(), sourcePage);
        Page<ExternalTransfer> externalTransfers = externalTransferRepository.findByUser_Id(user.getId(), sourcePage);
        List<TransferResponse> combined = Stream.concat(
                        safeBankTransfers.stream().map(transfer -> toSafeBankHistoryResponse(transfer, user.getId())),
                        externalTransfers.stream().map(this::toExternalHistoryResponse))
                .sorted(Comparator.comparing(TransferResponse::createdAt).reversed())
                .skip((long) page * size)
                .limit(size)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(combined, requestedPage,
                safeBankTransfers.getTotalElements() + externalTransfers.getTotalElements());
    }

    private TransferResponse toSafeBankHistoryResponse(Transfer transfer, Long userId) {
        return new TransferResponse(transfer.getId(), transfer.getSender().getAccountNumber(),
                transfer.getReceiver().getAccountNumber(), transfer.getSender().getName(),
                transfer.getReceiver().getName(), transfer.getAmount(), transfer.getDescription(), transfer.getStatus(), transfer.getCreatedAt(),
                transfer.getSender().getId().equals(userId) ? TransactionType.DEBIT : TransactionType.CREDIT,
                TransferChannel.SAFE_BANK_TRANSFER, null, null, null, null);
    }

    private TransferResponse toExternalHistoryResponse(ExternalTransfer transfer) {
        String bankName = transfer.getBankName();
        if (bankName == null || bankName.isBlank()) {
            bankName = transfer.getBankCode();
        }

        return new TransferResponse(transfer.getId(), transfer.getUser().getAccountNumber(), transfer.getAccountNumber(),
                transfer.getUser().getName(), transfer.getAccountName(), transfer.getAmount(), transfer.getNarration(),
                transfer.getStatus(), transfer.getCreatedAt(), TransactionType.DEBIT,
                transfer.getTransferChannel() == null ? TransferChannel.EXTERNAL_BANK_TRANSFER : transfer.getTransferChannel(),
                bankName, transfer.getBankCode(),
                transfer.getReference(), transfer.getTransferCode());
    }
}
