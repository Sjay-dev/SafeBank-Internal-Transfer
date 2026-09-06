package com.example.SafeBank.Config;

import com.example.SafeBank.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Creates demo users in the configured database only when they do not yet exist. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "demo.accounts", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoAccountSeeder implements ApplicationRunner {

    private static final DemoAccount[] DEMO_ACCOUNTS = {
            new DemoAccount("Joseph Sanusi", "joseph@demo.com"),
            new DemoAccount("Mary", "mary@demo.com"),
            new DemoAccount("Chris", "chris@demo.com")
    };

    private final AuthService authService;
    private final DemoBalanceProperties demoBalanceProperties;

    @Override
    public void run(ApplicationArguments args) {
        for (DemoAccount account : DEMO_ACCOUNTS) {
            authService.createUserIfMissing(
                    account.name(),
                    account.email(),
                    demoBalanceProperties.getAccounts().getPassword()
            );
        }
    }

    private record DemoAccount(String name, String email) {
    }
}
