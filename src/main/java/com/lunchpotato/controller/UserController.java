package com.lunchpotato.controller;

import com.lunchpotato.dto.RegisterRequest;
import com.lunchpotato.dto.UserResponse;
import com.lunchpotato.entity.User;
import com.lunchpotato.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UserResponse(user));
    }
}