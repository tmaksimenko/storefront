package com.tmaksimenko.storefront;

import com.tmaksimenko.storefront.exception.AccountNotFoundException;
import com.tmaksimenko.storefront.model.Account;
import com.tmaksimenko.storefront.model.Audit;
import com.tmaksimenko.storefront.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.tmaksimenko.storefront.enums.Role.ROLE_ADMIN;

@SpringBootApplication
public class StorefrontApplication {

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	AccountRepository accountRepository;

	public static void main(String[] args) {
		SpringApplication.run(StorefrontApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner() { return args -> {
		try {
			accountRepository.delete(Optional.ofNullable(accountRepository.findByUsername("Admin").isEmpty() ?
					null : accountRepository.findByUsername("Admin").get(0)).orElseThrow(AccountNotFoundException::new));
		} catch (AccountNotFoundException e) {
			System.out.println("Account not found");
		}

		Account account = Account.builder()
				.username("Admin")
				.email("admin@mail.com")
				.password(passwordEncoder.encode("password"))
				.role(ROLE_ADMIN)
				.audit(Audit.builder().createdOn(LocalDateTime.now()).createdBy("Admin").build())
				.build();
		accountRepository.save(account);
	};
	}

}
