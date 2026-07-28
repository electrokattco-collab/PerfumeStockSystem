package com.perfumestock.backend.config;

import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SeedData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User("admin", "admin@arthurford.co.za",
                passwordEncoder.encode("admin123"));
            userRepository.save(admin);
            System.out.println("=== Seeded admin user (admin / admin123) ===");
        }
    }
}
