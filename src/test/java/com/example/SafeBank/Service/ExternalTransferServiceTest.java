package com.example.SafeBank.Service;

import com.example.SafeBank.DTO.Request.ExternalTransferRequest;
import com.example.SafeBank.DTO.Response.ExternalTransferResponse;
import com.example.SafeBank.DTO.Response.PaystackAccountResolution;
import com.example.SafeBank.DTO.Response.PaystackRecipientResponse;
import com.example.SafeBank.Entities.Beneficiary;
import com.example.SafeBank.Entities.Enum.TransferStatus;
import com.example.SafeBank.Entities.ExternalTransfer;
import com.example.SafeBank.Entities.User;
import com.example.SafeBank.Repository.BeneficiaryRepository;
import com.example.SafeBank.Repository.ExternalTransferRepository;
import com.example.SafeBank.Repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExternalTransferServiceTest {

    @Test
    void resolvesAndCreatesARealRecipientBeforePersistingALocalSuccessfulTransfer() {
        UserRepository userRepository = mock(UserRepository.class);
        BeneficiaryRepository beneficiaryRepository = mock(BeneficiaryRepository.class);
        ExternalTransferRepository externalTransferRepository = mock(ExternalTransferRepository.class);
        ExternalBankTransferGateway transferGateway = mock(ExternalBankTransferGateway.class);
        BankCatalogService bankCatalogService = mock(BankCatalogService.class);
        PaystackService paystackService = mock(PaystackService.class);
        ExternalTransferService service = new ExternalTransferService(
                userRepository,
                beneficiaryRepository,
                externalTransferRepository,
                transferGateway,
                bankCatalogService,
                paystackService
        );
        User user = User.builder()
                .id(1L)
                .email("sender@safebank.test")
                .balance(new BigDecimal("1000.00"))
                .build();
        ExternalTransferRequest request = new ExternalTransferRequest(
                new BigDecimal("125.50"), "058", "0123456789", "Test payment");

        doNothing().when(bankCatalogService).requireKnownBankCode("058");
        when(userRepository.findByEmailForUpdate(user.getEmail())).thenReturn(Optional.of(user));
        when(paystackService.resolveAccount(request.accountNumber(), request.bankCode()))
                .thenReturn(new PaystackAccountResolution(request.accountNumber(), "Test Recipient"));
        when(beneficiaryRepository.findByUser_IdAndAccountNumberAndBankCode(1L, request.accountNumber(), request.bankCode()))
                .thenReturn(Optional.empty());
        when(paystackService.createRecipient("Test Recipient", request.accountNumber(), request.bankCode()))
                .thenReturn(new PaystackRecipientResponse("RCP_testrecipient"));
        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transferGateway.executeSuccessfulTransfer())
                .thenReturn(new MockTransferResult("safebank_test_reference", "TRF_test_code", TransferStatus.SUCCESS));
        when(externalTransferRepository.save(any(ExternalTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ExternalTransferResponse response = service.createTransfer(user.getEmail(), request);

        assertEquals(new BigDecimal("874.50"), user.getBalance());
        assertEquals("safebank_test_reference", response.reference());
        assertEquals("TRF_test_code", response.transferCode());
        assertEquals(TransferStatus.SUCCESS, response.status());
        verify(paystackService).resolveAccount(request.accountNumber(), request.bankCode());
        verify(paystackService).createRecipient("Test Recipient", request.accountNumber(), request.bankCode());
        verify(transferGateway).executeSuccessfulTransfer();
    }
}
