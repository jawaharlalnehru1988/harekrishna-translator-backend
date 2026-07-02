package com.harekrishna.translator;

import com.harekrishna.translator.model.Role;
import com.harekrishna.translator.model.User;
import com.harekrishna.translator.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class TranslatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TranslatorApplication.class, args);
	}

	@Bean
	public CommandLineRunner dataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.findByUsername("narasimha").isEmpty()) {
				User admin = User.builder()
						.username("narasimha")
						.password(passwordEncoder.encode("Bala#$88"))
						.role(Role.ADMIN)
						.approved(true)
						.build();
				userRepository.save(admin);
				System.out.println("Admin user created successfully.");
			}
		};
	}
}
