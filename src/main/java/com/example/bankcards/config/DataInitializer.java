package com.example.bankcards.config;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Инициализация начальных данных (администратор)
 */
@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Проверяем, есть ли уже администратор
        if (userRepository.findByUsername("admin").isEmpty()) {
            // Создаём администратора
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@bank.com");
            admin.setFullName("Administrator");

            Set<UserRole> roles = new HashSet<>();
            roles.add(UserRole.ADMIN);
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.println("Администратор создан: username=admin, password=admin123");
        }

        // Создаём тестового пользователя
        if (userRepository.findByUsername("user").isEmpty()) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@bank.com");
            user.setFullName("Test User");

            Set<UserRole> roles = new HashSet<>();
            roles.add(UserRole.USER);
            user.setRoles(roles);

            userRepository.save(user);
            System.out.println("Тестовый пользователь создан: username=user, password=user123");
        }
    }
}

