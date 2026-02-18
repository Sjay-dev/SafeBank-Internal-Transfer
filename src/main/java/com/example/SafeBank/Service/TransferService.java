package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Request.TransferRequest;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import com.example.SafeBank.DTO.Response.TransferResponse;
import com.example.SafeBank.Entities.Enum.TransferStatus;
import com.example.SafeBank.Entities.Transfer;
import com.example.SafeBank.Entities.User;
import com.example.SafeBank.Repository.TransferRepository;
import com.example.SafeBank.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final UserRepository userRepository;
    private final TransferRepository transferRepository;

    @Transactional
    public TransferResponse performTransfer(TransferRequest request) {

        // 1️⃣ Get sender
        User sender = userRepository.findByAccountNumber(request.senderAccountNumber())
                .orElseThrow(() ->
                        new CustomExceptions.UserNotFoundException("Sender not found"));

        // 2️⃣ Get receiver
        User receiver = userRepository.findByAccountNumber(request.receiverAccountNumber())
                .orElseThrow(() ->
                        new CustomExceptions.UserNotFoundException("Receiver not found"));

        // 3️⃣ Prevent self transfer
        if (sender.getAccountNumber().equals(receiver.getAccountNumber())) {
            throw new CustomExceptions.InvalidTransferException("Cannot transfer to yourself");
        }

        // 4️⃣ Check balance
        if (sender.getBalance().compareTo(request.amount()) < 0) {
            throw new CustomExceptions.InsufficientBalanceException("Insufficient balance");
        }

        // 5️⃣ Deduct & Add
        sender.setBalance(sender.getBalance().subtract(request.amount()));
        receiver.setBalance(receiver.getBalance().add(request.amount()));

        userRepository.save(sender);
        userRepository.save(receiver);

        // 6️⃣ Save transfer record
        Transfer transfer = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(request.amount())
                .description(request.description())
                .status(TransferStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        Transfer savedTransfer = transferRepository.save(transfer);

        // 7️⃣ Return response
        return new TransferResponse(
                savedTransfer.getId(),
                sender.getAccountNumber(),
                receiver.getAccountNumber(),
                savedTransfer.getAmount(),
                savedTransfer.getDescription(),
                savedTransfer.getStatus(),
                savedTransfer.getCreatedAt()
        );
    }

}
