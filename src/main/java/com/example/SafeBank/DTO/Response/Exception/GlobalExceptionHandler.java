package com.example.SafeBank.DTO.Response.Exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomExceptions.UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(CustomExceptions.UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(CustomExceptions.InsufficientBalanceException.class)
    public ResponseEntity<String> handleInsufficientBalance(CustomExceptions.InsufficientBalanceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CustomExceptions.InvalidTransferException.class)
    public ResponseEntity<String> handleInvalidTransfer(CustomExceptions.InvalidTransferException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CustomExceptions.AuthenticationFailedException.class)
    public ResponseEntity<String> handleAuthenticationFailed(CustomExceptions.AuthenticationFailedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(CustomExceptions.PaystackException.class)
    public ResponseEntity<String> handlePaystackError(CustomExceptions.PaystackException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Paystack error: " + ex.getMessage());
    }

    @ExceptionHandler(CustomExceptions.RecipientCreationException.class)
    public ResponseEntity<String> handleRecipientCreationFailed(CustomExceptions.RecipientCreationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CustomExceptions.TransferFailedException.class)
    public ResponseEntity<String> handleTransferFailed(CustomExceptions.TransferFailedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
    }
}
