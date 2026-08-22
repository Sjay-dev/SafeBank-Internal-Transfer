package com.example.SafeBank.Service;

import com.example.SafeBank.Entities.Enum.TransferStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockExternalBankTransferGatewayTest {

    private final ExternalBankTransferGateway gateway = new MockExternalBankTransferGateway();

    @Test
    void generatesAPersistableSuccessfulTransferResult() {
        MockTransferResult result = gateway.executeSuccessfulTransfer();

        assertEquals(TransferStatus.SUCCESS, result.status());
        assertTrue(result.reference().matches("safebank_[0-9a-f-]{36}"));
        assertTrue(result.transferCode().matches("TRF_[0-9A-F]{32}"));
    }
}
