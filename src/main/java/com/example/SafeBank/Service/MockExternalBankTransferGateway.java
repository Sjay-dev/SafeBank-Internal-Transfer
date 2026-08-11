package com.example.SafeBank.Service;

import com.example.SafeBank.Entities.Enum.TransferStatus;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class MockExternalBankTransferGateway implements ExternalBankTransferGateway {

    @Override
    public MockTransferResult executeSuccessfulTransfer() {
        return new MockTransferResult(
                "safebank_" + UUID.randomUUID(),
                "TRF_" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT),
                TransferStatus.SUCCESS
        );
    }
}
