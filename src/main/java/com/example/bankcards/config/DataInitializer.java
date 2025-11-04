package com.example.bankcards.config;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	@Override
	public void run(String... args) {
		try {
			initializeAdmin();
			initializeUser();
		} catch (Exception e) {
			log.error("Ошибка при инициализации начальных данных", e);
		}
	}

	private void initializeAdmin() {
		if (userRepository.findByUsername("admin").isEmpty()) {
			try {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setEmail("admin@bank.com");
				admin.setFullName("Administrator");
				admin.setRoles(Set.of(UserRole.ADMIN));
				userRepository.save(admin);
				log.info("Создан пользователь-администратор: admin");
			} catch (Exception e) {
				log.error("Не удалось создать пользователя-администратора", e);
			}
		} else {
			log.debug("Пользователь-администратор уже существует: admin");
		}
	}

	private void initializeUser() {
		if (userRepository.findByUsername("user").isEmpty()) {
			try {
				User user = new User();
				user.setUsername("user");
				user.setPassword(passwordEncoder.encode("user123"));
				user.setEmail("user@bank.com");
				user.setFullName("Test User");
				user.setRoles(Set.of(UserRole.USER));
				userRepository.save(user);
				log.info("Создан тестовый пользователь: user");
			} catch (Exception e) {
				log.error("Не удалось создать тестового пользователя", e);
			}
		} else {
			log.debug("Тестовый пользователь уже существует: user");
		}
	}
}


