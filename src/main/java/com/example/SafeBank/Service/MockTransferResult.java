package com.example.SafeBank.Service;

import com.example.SafeBank.Entities.Enum.TransferStatus;

public record MockTransferResult(String reference, String transferCode, TransferStatus status) {
}
