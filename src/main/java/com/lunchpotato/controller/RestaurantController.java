
package com.lunchpotato.controller;

import jakarta.validation.Valid;
import com.lunchpotato.entity.Restaurant;
import com.lunchpotato.service.RestaurantService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PreAuthorize("hasRole('ADMIN')")

    // Add a new restaurant
    @PostMapping
    public Restaurant addRestaurant(@Valid @RequestBody Restaurant restaurant) {
        return restaurantService.addRestaurant(restaurant);
    }

    // Get all restaurants
    @GetMapping
    public List<Restaurant> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    // Get restaurant by ID
    @GetMapping("/{id}")
    public Restaurant getRestaurantById(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")

    // Delete restaurant
    @DeleteMapping("/{id}")
    public String deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return "Restaurant deleted successfully!";
    }
}
