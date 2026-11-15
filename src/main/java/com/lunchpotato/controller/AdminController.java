package com.lunchpotato.controller;

import com.lunchpotato.dto.AssignOwnerRequest;
import com.lunchpotato.dto.AdminOverviewResponse;
import com.lunchpotato.dto.AdminRestaurantResponse;
import com.lunchpotato.entity.Restaurant;
import com.lunchpotato.service.AdminService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lunchpotato.dto.RestaurantUpdateRequest;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/overview")
    public ResponseEntity<AdminOverviewResponse> getOverview() {
        return ResponseEntity.ok(adminService.getOverview());
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<AdminRestaurantResponse>> getAllRestaurants() {
        return ResponseEntity.ok(adminService.getAllRestaurants());
    }

    @PutMapping("/restaurants/{restaurantId}/owner")
    public ResponseEntity<Restaurant> assignOwner(
            @PathVariable Long restaurantId,
            @Valid @RequestBody AssignOwnerRequest request) {

        return ResponseEntity.ok(
                adminService.assignOwner(
                        restaurantId,
                        request.getEmail()));
    }

    @PutMapping("/restaurants/{restaurantId}")
    public ResponseEntity<Restaurant> updateRestaurant(
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantUpdateRequest request) {
        return ResponseEntity.ok(
                adminService.updateRestaurant(restaurantId, request));
    }

    @PostMapping(value = "/restaurants/{restaurantId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Restaurant> uploadRestaurantLogo(
            @PathVariable Long restaurantId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(
                adminService.uploadRestaurantLogo(restaurantId, file));
    }
}