package com.example.SafeBank.DTO.Response.Exception;

public class CustomExceptions {


    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String message) {
            super(message);
        }
    }


    public static class InvalidTransferException extends RuntimeException {
        public InvalidTransferException(String message) {
            super(message);
        }
    }


    public static class AuthenticationFailedException extends RuntimeException {
        public AuthenticationFailedException(String message) {
            super(message);
        }
    }


    public static class PaystackException extends RuntimeException {
        public PaystackException(String message) {
            super(message);
        }
    }


    public static class RecipientCreationException extends RuntimeException {
        public RecipientCreationException(String message) {
            super(message);
        }
    }

    public static class TransferFailedException extends RuntimeException {
        public TransferFailedException(String message) {
            super(message);
        }
    }

}
