package com.lunchpotato.dto;

import com.lunchpotato.entity.Role;

public class ProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private String address;

    public ProfileResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            String address,
            Role role) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public Role getRole() {
        return role;
    }
}