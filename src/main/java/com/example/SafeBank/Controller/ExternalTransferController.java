package com.example.SafeBank.Controller;

import com.example.SafeBank.DTO.Request.ExternalTransferRequest;
import com.example.SafeBank.DTO.Response.ExternalTransferResponse;
import com.example.SafeBank.Service.ExternalTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class ExternalTransferController {

    private final ExternalTransferService externalTransferService;

    @PostMapping("/external")
    public ExternalTransferResponse createExternalTransfer(Authentication authentication,
                                                           @Valid @RequestBody ExternalTransferRequest request) {
        return externalTransferService.createTransfer(authentication.getName(), request);
    }

    @GetMapping("/{reference}")
    public ExternalTransferResponse verifyExternalTransfer(Authentication authentication,
                                                           @PathVariable String reference) {
        return externalTransferService.verifyTransfer(authentication.getName(), reference);
    }
}
