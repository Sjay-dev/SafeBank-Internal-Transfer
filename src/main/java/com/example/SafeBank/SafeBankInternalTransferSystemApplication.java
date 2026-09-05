package com.example.SafeBank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.SafeBank.Config.BankCatalogProperties;
import com.example.SafeBank.Config.DemoBalanceProperties;

@SpringBootApplication
@EnableConfigurationProperties({BankCatalogProperties.class, DemoBalanceProperties.class})
public class SafeBankInternalTransferSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SafeBankInternalTransferSystemApplication.class, args);
	}

}
