package com.example.SafeBank.Controllers;

import com.example.SafeBank.DTO.Request.TransferRequest;
import com.example.SafeBank.DTO.Response.TransferResponse;
import com.example.SafeBank.Service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public TransferResponse performTransfer(
            @Valid @RequestBody TransferRequest request) {

        return transferService.performTransfer(request);
    }

    @GetMapping("/history")
    public Page<TransferResponse> getMyHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String email = authentication.getName();
        return transferService.getMyTransactionHistory(email, page, size);
    }

}
