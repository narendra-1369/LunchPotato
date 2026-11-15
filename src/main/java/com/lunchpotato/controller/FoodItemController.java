
package com.lunchpotato.controller;

import com.lunchpotato.dto.FoodItemRequest;
import com.lunchpotato.entity.FoodItem;
import com.lunchpotato.service.FoodItemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/food-items")
@CrossOrigin(origins = "*")
public class FoodItemController {

    private final FoodItemService foodItemService;

    public FoodItemController(FoodItemService foodItemService) {
        this.foodItemService = foodItemService;
    }

    @PreAuthorize("hasRole('ADMIN')")

    // Add a food item
    @PostMapping
    public FoodItem addFoodItem(@Valid @RequestBody FoodItemRequest request) {
        return foodItemService.addFoodItem(request);
    }

    @PreAuthorize("hasRole('ADMIN')")

    @PutMapping("/{id}")
    public FoodItem updateFoodItem(
            @PathVariable Long id,
            @Valid @RequestBody FoodItemRequest request) {

        return foodItemService.updateFoodItem(id, request);
    }

    // Get all food items
    @GetMapping
    public List<FoodItem> getAllFoodItems() {
        return foodItemService.getAllFoodItems();
    }

    @GetMapping("/search")
    public List<FoodItem> searchFoodItems(@RequestParam String name) {
        return foodItemService.searchFoodItems(name);
    }

    @GetMapping("/availability")
    public List<FoodItem> getFoodItemsByAvailability(
            @RequestParam boolean available) {
        return foodItemService.getFoodItemsByAvailability(available);
    }

    // Get food item by ID
    @GetMapping("/{id}")
    public FoodItem getFoodItemById(@PathVariable Long id) {
        return foodItemService.getFoodItemById(id);
    }

    // Get food items by restaurant ID
    @GetMapping("/restaurant/{restaurantId}")
    public List<FoodItem> getFoodItemsByRestaurant(
            @PathVariable Long restaurantId) {
        return foodItemService.getFoodItemsByRestaurant(restaurantId);
    }

    @PreAuthorize("hasRole('ADMIN')")

    // Delete food item
    @DeleteMapping("/{id}")
    public String deleteFoodItem(@PathVariable Long id) {
        foodItemService.deleteFoodItem(id);
        return "Food item deleted successfully!";
    }
}
