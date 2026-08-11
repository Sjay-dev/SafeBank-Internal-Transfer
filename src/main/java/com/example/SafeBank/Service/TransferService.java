package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Request.TransferRequest;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import com.example.SafeBank.DTO.Response.TransferResponse;
import com.example.SafeBank.Entities.Enum.TransactionType;
import com.example.SafeBank.Entities.Enum.TransferStatus;
import com.example.SafeBank.Entities.Transfer;
import com.example.SafeBank.Entities.User;
import com.example.SafeBank.Repository.TransferRepository;
import com.example.SafeBank.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final UserRepository userRepository;
    private final TransferRepository transferRepository;

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
                TransactionType.DEBIT
        );
    }

    public Page<TransferResponse> getMyTransactionHistory(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return transferRepository.findBySender_IdOrReceiver_Id(user.getId(), user.getId(), pageable)
                .map(transfer -> new TransferResponse(
                        transfer.getId(),
                        transfer.getSender().getAccountNumber(),
                        transfer.getReceiver().getAccountNumber(),
                        transfer.getSender().getName(),
                        transfer.getReceiver().getName(),
                        transfer.getAmount(),
                        transfer.getDescription(),
                        transfer.getStatus(),
                        transfer.getCreatedAt(),
                        transfer.getSender().getId().equals(user.getId())
                                ? TransactionType.DEBIT
                                : TransactionType.CREDIT
                ));
    }
}
