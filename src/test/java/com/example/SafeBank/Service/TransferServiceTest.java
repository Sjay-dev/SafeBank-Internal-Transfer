package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Request.TransferRequest;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import com.example.SafeBank.DTO.Response.TransferResponse;
import com.example.SafeBank.Entities.Enum.TransactionType;
import com.example.SafeBank.Entities.Enum.TransferStatus;
import com.example.SafeBank.Entities.User;
import com.example.SafeBank.Repository.TransferRepository;
import com.example.SafeBank.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

//@SpringBootTest
//@RequiredArgsConstructor
//class TransferServiceTest {
//
//    @Autowired
//    private TransferService transferService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private TransferRepository transferRepository;
//
//    private User sender;
//    private User receiver;
//
//    @BeforeEach
//    void setup() {
//        sender = userRepository.save(User.builder()
//                .name("Alice")
//                .email("alice@example.com")
//                .accountNumber("ACC001")
//                .balance(new BigDecimal("1000"))
//                .password("password")
//                .createdAt(LocalDateTime.now())
//                .build()
//        );
//
//        receiver = userRepository.save(User.builder()
//                .name("Bob")
//                .email("bob@example.com")
//                .accountNumber("ACC002")
//                .balance(new BigDecimal("500"))
//                .password("password")
//                .createdAt(LocalDateTime.now())
//                .build()
//        );
//    }
//
//
//    @Test
//    void testPerformTransferSuccess() {
//        TransferRequest request = new TransferRequest(
//                sender.getAccountNumber(),
//                receiver.getAccountNumber(),
//                new BigDecimal("200"),
//                "Payment"
//        );
//
//        TransferResponse response = transferService.performTransfer(request);
//
//        assertEquals(sender.getAccountNumber(), response.senderAccountNumber());
//        assertEquals(receiver.getAccountNumber(), response.receiverAccountNumber());
//        assertEquals(new BigDecimal("200"), response.amount());
//        assertEquals(TransferStatus.SUCCESS, response.status());
//
//        User updatedSender = userRepository.findByAccountNumber(sender.getAccountNumber()).get();
//        User updatedReceiver = userRepository.findByAccountNumber(receiver.getAccountNumber()).get();
//
//        assertEquals(new BigDecimal("800"), updatedSender.getBalance());
//        assertEquals(new BigDecimal("700"), updatedReceiver.getBalance());
//    }
//
//    @Test
//    void testPerformTransferInsufficientBalance() {
//        TransferRequest request = new TransferRequest(
//                sender.getAccountNumber(),
//                receiver.getAccountNumber(),
//                new BigDecimal("2000"),
//                "Payment"
//        );
//
//        assertThrows(CustomExceptions.InsufficientBalanceException.class,
//                () -> transferService.performTransfer(request));
//    }
//
//    @Test
//    void testPerformTransferSelfTransfer() {
//        TransferRequest request = new TransferRequest(
//                sender.getAccountNumber(),
//                sender.getAccountNumber(),
//                new BigDecimal("100"),
//                "Self transfer"
//        );
//
//        assertThrows(CustomExceptions.InvalidTransferException.class,
//                () -> transferService.performTransfer(request));
//    }
//
//    @Test
//    void testTransferHistory() {
//        // Create two transfers
//        transferService.performTransfer(new TransferRequest(
//                sender.getAccountNumber(),
//                receiver.getAccountNumber(),
//                new BigDecimal("100"),
//                "Payment1"
//        ));
//
//        transferService.performTransfer(new TransferRequest(
//                receiver.getAccountNumber(),
//                sender.getAccountNumber(),
//                new BigDecimal("50"),
//                "Payment2"
//        ));
//
//        Page<TransferResponse> history = transferService.getMyTransactionHistory(
//                sender.getEmail(), 0, 10
//        );
//
//        assertEquals(2, history.getTotalElements());
//        assertEquals(TransactionType.DEBIT, history.getContent().get(0).transactionType());
//        assertEquals(TransactionType.CREDIT, history.getContent().get(1).transactionType());
//    }
//}

