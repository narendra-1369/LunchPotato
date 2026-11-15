package com.lunchpotato.controller;

import com.lunchpotato.dto.ProfileRequest;
import com.lunchpotato.dto.ProfileResponse;
import com.lunchpotato.service.ProfileService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            Authentication authentication) {

        return ResponseEntity.ok(
                profileService.getProfile(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody ProfileRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                profileService.updateProfile(
                        authentication.getName(),
                        request));
    }
}