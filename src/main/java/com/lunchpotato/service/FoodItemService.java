
package com.lunchpotato.service;

import com.lunchpotato.dto.FoodItemRequest;
import com.lunchpotato.entity.FoodItem;
import com.lunchpotato.repository.FoodItemRepository;
import com.lunchpotato.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final RestaurantRepository restaurantRepository;

    public FoodItemService(
            FoodItemRepository foodItemRepository,
            RestaurantRepository restaurantRepository) {
        this.foodItemRepository = foodItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    // Add a new food item
    public FoodItem addFoodItem(FoodItemRequest request) {

        var restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        FoodItem foodItem = new FoodItem();

        foodItem.setName(request.getName());
        foodItem.setDescription(request.getDescription());
        foodItem.setPrice(request.getPrice());
        foodItem.setAvailable(request.getAvailable());
        foodItem.setRestaurant(restaurant);

        return foodItemRepository.save(foodItem);
    }

    // update a food item
    public FoodItem updateFoodItem(Long id, FoodItemRequest request) {

        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food item not found"));

        var restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        foodItem.setName(request.getName());
        foodItem.setDescription(request.getDescription());
        foodItem.setPrice(request.getPrice());
        foodItem.setAvailable(request.getAvailable());
        foodItem.setRestaurant(restaurant);

        return foodItemRepository.save(foodItem);
    }

    // Get all food items
    public List<FoodItem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }

    public List<FoodItem> searchFoodItems(String name) {
        return foodItemRepository.findByNameContainingIgnoreCase(name);
    }

    public List<FoodItem> getFoodItemsByAvailability(boolean available) {
        return foodItemRepository.findByAvailable(available);
    }

    // Get food item by ID
    public FoodItem getFoodItemById(Long id) {
        return foodItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food item not found"));
    }

    // Get menu of a particular restaurant
    public List<FoodItem> getFoodItemsByRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RuntimeException("Restaurant not found");
        }

        return foodItemRepository.findByRestaurantId(restaurantId);
    }

    // Delete food item
    public void deleteFoodItem(Long id) {
        if (!foodItemRepository.existsById(id)) {
            throw new RuntimeException("Food item not found");
        }

        foodItemRepository.deleteById(id);
    }
}
