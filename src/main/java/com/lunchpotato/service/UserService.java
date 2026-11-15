package com.lunchpotato.service;

import com.lunchpotato.dto.RegisterRequest;
import com.lunchpotato.entity.Role;
import com.lunchpotato.entity.User;
import com.lunchpotato.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public User registerUser(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists");
        }

        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone().trim());
        user.setAddress(request.getAddress().trim());

        // Encrypt the password before saving
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));

        // Every public registration creates a customer account
        user.setRole(Role.CUSTOMER);

        return userRepository.save(user);
    }
}