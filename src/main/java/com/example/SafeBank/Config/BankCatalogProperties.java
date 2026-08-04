package com.example.SafeBank.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "bank-catalog")
public class BankCatalogProperties {
    /** Maximum age of the locally stored Paystack catalogue before it is refreshed. */
    private Duration refreshInterval = Duration.ofHours(24);

    public Duration getRefreshInterval() { return refreshInterval; }
    public void setRefreshInterval(Duration refreshInterval) { this.refreshInterval = refreshInterval; }
}
