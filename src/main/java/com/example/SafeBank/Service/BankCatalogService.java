package com.example.SafeBank.Service;

import com.example.SafeBank.Config.BankCatalogProperties;
import com.example.SafeBank.DTO.Response.BankListItem;
import com.example.SafeBank.DTO.Response.Exception.CustomExceptions;
import com.example.SafeBank.Entities.Bank;
import com.example.SafeBank.Repository.BankRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class BankCatalogService {
    private final BankRepository bankRepository;
    private final PaystackService paystackService;
    private final BankCatalogProperties properties;

    public BankCatalogService(BankRepository bankRepository, PaystackService paystackService,
                              BankCatalogProperties properties) {
        this.bankRepository = bankRepository;
        this.paystackService = paystackService;
        this.properties = properties;
    }

    @Transactional
    public List<BankListItem> getNigerianBanks() {

        List<Bank> storedBanks = bankRepository.findAllByOrderByNameAsc();

        if (isFresh(storedBanks)) {
            return toItems(storedBanks);
        }

        try {
            refreshFromProvider();
            return toItems(bankRepository.findAllByOrderByNameAsc());
        } catch (CustomExceptions.PaystackException ex) {
            if (!storedBanks.isEmpty()) return toItems(storedBanks);
            throw ex;
        }
    }

    /** Ensures account-resolution and transfer requests only use a backend-issued bank code. */
    public void requireKnownBankCode(String bankCode) {
        getNigerianBanks();
        if (bankCode == null || bankRepository.findById(bankCode).isEmpty()) {
            throw new CustomExceptions.InvalidTransferException("Unknown bank code");
        }
    }

    private boolean isFresh(List<Bank> storedBanks) {
        if (storedBanks.isEmpty()) return false;
        return bankRepository.findTopByOrderByLastRefreshedAtDesc()
                .map(bank -> !bank.getLastRefreshedAt().isBefore(LocalDateTime.now().minus(properties.getRefreshInterval())))
                .orElse(false);
    }

    private void refreshFromProvider() {
        LocalDateTime refreshedAt = LocalDateTime.now();
        List<Bank> banks = paystackService.getAllBanks().stream()
                .filter(bank -> bank.getCode() != null && !bank.getCode().isBlank())
                .filter(bank -> bank.getName() != null && !bank.getName().isBlank())
                .map(bank -> new Bank(bank.getCode(), bank.getName(), refreshedAt))
                .toList();
        if (banks.isEmpty()) throw new CustomExceptions.PaystackException("Paystack returned no Nigerian banks");
        bankRepository.saveAll(banks);
    }

    private List<BankListItem> toItems(List<Bank> banks) {
        return banks.stream()
                .map(bank -> new BankListItem(bank.getName(), bank.getCode()))
                .sorted(Comparator.comparing(BankListItem::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
