package com.example.SafeBank.Controllers;

import com.example.SafeBank.DTO.Request.TransferRequest;
import com.example.SafeBank.DTO.Response.TransferResponse;
import com.example.SafeBank.Service.TransferService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Validated
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public TransferResponse performTransfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request) {
        return transferService.performTransfer(authentication.getName(), request);
    }

    @GetMapping("/history")
    public Page<TransferResponse> getMyHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        String email = authentication.getName();
        return transferService.getMyTransactionHistory(email, page, size);
    }
}
