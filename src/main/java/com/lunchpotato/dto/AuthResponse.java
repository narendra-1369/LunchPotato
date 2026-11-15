package com.lunchpotato.dto;

public class AuthResponse {

    private String token;
    private String tokenType;
    private long expiresIn;
    private Long userId;
    private String fullName;
    private String email;
    private String role;

    public AuthResponse(
            String token,
            long expiresIn,
            Long userId,
            String fullName,
            String email,
            String role) {

        this.token = token;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}