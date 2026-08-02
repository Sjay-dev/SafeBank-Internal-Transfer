package com.example.SafeBank.Controller;

import com.example.SafeBank.DTO.Response.PaystackApiResponse;
import com.example.SafeBank.DTO.Response.BankResponse;
import com.example.SafeBank.Service.PaystackService;
import com.example.SafeBank.Service.ExternalTransferService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/banks")
@Validated
public class PaystackController {

    private final PaystackService paystackService;
    private final ExternalTransferService externalTransferService;

    public PaystackController(PaystackService paystackService, ExternalTransferService externalTransferService) {
        this.paystackService = paystackService;
        this.externalTransferService = externalTransferService;
    }

    @GetMapping
    public ResponseEntity<PaystackApiResponse<java.util.List<BankResponse>>> getBanks() {
        var response = paystackService.getAllBanks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resolve")
    public com.example.SafeBank.DTO.Response.AccountResolutionResponse resolveAccount(
            @RequestParam("account_number") @Pattern(regexp = "\\d{10}") String accountNumber,
            @RequestParam("bank_code") @NotBlank String bankCode) {
        return externalTransferService.resolveAccount(accountNumber, bankCode);
    }
}
