package com.example.SafeBank.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "external-transfer")
public class ExternalTransferProviderProperties {
    /** mock for local development; paystack for production. */
    private String provider = "mock";
    private final Mock mock = new Mock();

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public Mock getMock() { return mock; }

    public static class Mock {
        /** Transfers to these account numbers return a failed provider status. */
        private Set<String> failedAccountNumbers = new HashSet<>(Set.of("0000000000"));

        public Set<String> getFailedAccountNumbers() { return failedAccountNumbers; }
        public void setFailedAccountNumbers(Set<String> failedAccountNumbers) {
            this.failedAccountNumbers = failedAccountNumbers == null ? new HashSet<>() : new HashSet<>(failedAccountNumbers);
        }
    }
}
