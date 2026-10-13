package com.autocare.config;

import com.autocare.model.Role;
import com.autocare.model.User;
import com.autocare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.username:}")
    private String username;

    @Value("${app.bootstrap-admin.email:}")
    private String email;

    @Value("${app.bootstrap-admin.password:}")
    private String password;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        boolean configured = !username.isBlank() || !email.isBlank() || !password.isBlank();
        if (!configured) return;
        if (username.isBlank() || email.isBlank() || password.length() < 12) {
            throw new IllegalStateException("Bootstrap admin requires username, email, and a password of at least 12 characters.");
        }

        var existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getEmail().equalsIgnoreCase(email) && user.getRole() == Role.ROLE_ADMIN) return;
            throw new IllegalStateException("Bootstrap admin username is already associated with another account.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Bootstrap admin email is already associated with another account.");
        }

        userRepository.save(new User(username, email, passwordEncoder.encode(password), Role.ROLE_ADMIN));
    }
}
