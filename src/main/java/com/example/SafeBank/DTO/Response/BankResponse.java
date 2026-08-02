package com.example.SafeBank.DTO.Response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BankResponse {
    // Getters and setters
    private String code;
    private String name;
    private String sortCode;
    private boolean isDefault;
    private boolean isMobileMoney;
    private String bin;

}