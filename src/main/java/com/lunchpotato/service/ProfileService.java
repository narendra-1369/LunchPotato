package com.lunchpotato.service;

import com.lunchpotato.dto.ProfileRequest;
import com.lunchpotato.dto.ProfileResponse;
import com.lunchpotato.entity.User;
import com.lunchpotato.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return convertToResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(
            String email,
            ProfileRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        user.setAddress(request.getAddress().trim());

        User updatedUser = userRepository.save(user);

        return convertToResponse(updatedUser);
    }

    private ProfileResponse convertToResponse(User user) {

        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole());

    }
}