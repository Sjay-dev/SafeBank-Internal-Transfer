package com.example.SafeBank.DTO.Response;

/** Stable API payload consumed by bank-selection clients. */
public record BankListItem(String name, String code) { }
