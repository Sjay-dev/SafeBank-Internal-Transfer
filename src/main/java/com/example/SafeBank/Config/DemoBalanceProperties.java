package com.example.SafeBank.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "demo")
public class DemoBalanceProperties {

    private BigDecimal startingBalance = new BigDecimal("30000.00");
    private Accounts accounts = new Accounts();

    @Getter
    @Setter
    public static class Accounts {
        private boolean enabled = true;
        private String password = "Password123";
    }
}
