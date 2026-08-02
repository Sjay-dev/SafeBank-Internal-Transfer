package com.example.SafeBank.DTO.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PaystackApiResponse<T> {
    private boolean status;
    private String message;
    private T data;

}