package com.example.SafeBank.DTO.Response.Exception;

public class CustomExceptions {

    // Thrown when a user is not found in the system
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    // Thrown when sender doesn't have enough balance
    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String message) {
            super(message);
        }
    }

    // Thrown when a transfer is invalid (e.g., sending to self)
    public static class InvalidTransferException extends RuntimeException {
        public InvalidTransferException(String message) {
            super(message);
        }
    }

    // Thrown on authentication failures
    public static class AuthenticationFailedException extends RuntimeException {
        public AuthenticationFailedException(String message) {
            super(message);
        }
    }

    // Thrown when Paystack API returns an error
    public static class PaystackException extends RuntimeException {
        public PaystackException(String message) {
            super(message);
        }
    }

    // Thrown when recipient creation fails
    public static class RecipientCreationException extends RuntimeException {
        public RecipientCreationException(String message) {
            super(message);
        }
    }

    // Thrown when transfer fails
    public static class TransferFailedException extends RuntimeException {
        public TransferFailedException(String message) {
            super(message);
        }
    }

}
