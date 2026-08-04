package com.example.SafeBank.Controller;

import com.example.SafeBank.DTO.Response.BankListItem;
import com.example.SafeBank.Service.BankCatalogService;
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
@RequestMapping({"/banks", "/api/banks"})
@Validated
public class PaystackController {

    private final BankCatalogService bankCatalogService;
    private final ExternalTransferService externalTransferService;

    public PaystackController(BankCatalogService bankCatalogService, ExternalTransferService externalTransferService) {
        this.bankCatalogService = bankCatalogService;
        this.externalTransferService = externalTransferService;
    }

    @GetMapping
    public ResponseEntity<java.util.List<BankListItem>> getBanks() {
        return ResponseEntity.ok(bankCatalogService.getNigerianBanks());
    }

    @GetMapping("/resolve")
    public com.example.SafeBank.DTO.Response.AccountResolutionResponse resolveAccount(
            @RequestParam("account_number") @Pattern(regexp = "\\d{10}") String accountNumber,
            @RequestParam("bank_code") @NotBlank String bankCode) {
        return externalTransferService.resolveAccount(accountNumber, bankCode);
    }
}
