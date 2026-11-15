package com.lunchpotato.controller;

import com.lunchpotato.entity.*;
import com.lunchpotato.service.OwnerService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.lunchpotato.dto.FoodItemRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/api/owner")
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<Restaurant>> getMyRestaurants(
            Authentication authentication) {

        return ResponseEntity.ok(
                ownerService.getMyRestaurants(authentication.getName()));
    }

    @GetMapping("/menu")
    public ResponseEntity<List<FoodItem>> getMyMenu(
            Authentication authentication) {

        return ResponseEntity.ok(
                ownerService.getMyMenu(authentication.getName()));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getMyOrders(
            Authentication authentication) {

        return ResponseEntity.ok(
                ownerService.getMyOrders(authentication.getName()));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            Authentication authentication) {

        return ResponseEntity.ok(
                ownerService.updateOrderStatus(
                        orderId,
                        status,
                        authentication.getName()));
    }

    @PostMapping("/menu")
    public ResponseEntity<FoodItem> addFoodItem(
            @Valid @RequestBody FoodItemRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ownerService.addFoodItem(
                        request, authentication.getName()));
    }

    @PutMapping("/menu/{foodItemId}")
    public ResponseEntity<FoodItem> updateFoodItem(
            @PathVariable Long foodItemId,
            @Valid @RequestBody FoodItemRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ownerService.updateFoodItem(
                        foodItemId, request, authentication.getName()));
    }

    @DeleteMapping("/menu/{foodItemId}")
    public ResponseEntity<Void> deleteFoodItem(
            @PathVariable Long foodItemId,
            Authentication authentication) {

        ownerService.deleteFoodItem(
                foodItemId, authentication.getName());

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/menu/{foodItemId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodItem> uploadFoodImage(
            @PathVariable Long foodItemId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        return ResponseEntity.ok(
                ownerService.uploadFoodImage(
                        foodItemId,
                        file,
                        authentication.getName()));
    }
}