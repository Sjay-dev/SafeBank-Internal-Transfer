package com.example.SafeBank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.SafeBank.Config.ExternalTransferProviderProperties;
import com.example.SafeBank.Config.BankCatalogProperties;

@SpringBootApplication
@EnableConfigurationProperties({ExternalTransferProviderProperties.class, BankCatalogProperties.class})
public class SafeBankInternalTransferSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SafeBankInternalTransferSystemApplication.class, args);
	}

}
