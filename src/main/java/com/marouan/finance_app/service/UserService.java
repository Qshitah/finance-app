package com.marouan.finance_app.service;

import com.marouan.finance_app.domain.User;
import com.marouan.finance_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signup(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        // constructor defaults enabled=false, role=USER, so this account can't log in until an admin enables it
        var user = new User(username, email, passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }
}