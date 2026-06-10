package com.example.hospitalmanagement.config;

import com.example.hospitalmanagement.entity.User;
import com.example.hospitalmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Tự động tạo tài khoản ADMIN nếu chưa có
            if (!userRepository.existsByUsername("admin01")) {
                User admin = User.builder()
                        .username("admin01")
                        .password(passwordEncoder.encode("123456"))
                        .email("admin@hospital.com")
                        .fullName("Super Admin")
                        .phone("0900000000")
                        .role(User.Role.ADMIN)
                        .status(User.UserStatus.ACTIVE)
                        .build();
                userRepository.save(admin);
                log.info(">>> [INIT] Admin account created: username=admin01 / password=123456");
            } else {
                log.info(">>> [INIT] Admin account already exists, skipping.");
            }
        };
    }
}