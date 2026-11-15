package com.lunchpotato.service;

import com.lunchpotato.entity.*;
import com.lunchpotato.repository.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunchpotato.dto.FoodItemRequest;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class OwnerService {

    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;
    private final OrderRepository orderRepository;
    private final FoodImageStorageService foodImageStorageService;
    private final CartItemRepository cartItemRepository;

    public OwnerService(
            RestaurantRepository restaurantRepository,
            FoodItemRepository foodItemRepository,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            FoodImageStorageService foodImageStorageService) {

        this.restaurantRepository = restaurantRepository;
        this.foodItemRepository = foodItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.foodImageStorageService = foodImageStorageService;
    }

    private Restaurant getOwnedRestaurant(Long restaurantId, String email) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (restaurant.getOwner() == null ||
                !restaurant.getOwner().getEmail().equalsIgnoreCase(email)) {

            throw new AccessDeniedException(
                    "You do not own this restaurant");
        }

        return restaurant;
    }

    private FoodItem getOwnedFoodItem(Long foodItemId, String email) {

        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new RuntimeException("Food item not found"));

        if (foodItem.getRestaurant().getOwner() == null ||
                !foodItem.getRestaurant().getOwner().getEmail()
                        .equalsIgnoreCase(email)) {

            throw new AccessDeniedException(
                    "You cannot manage another restaurant's food items");
        }

        return foodItem;
    }

    @Transactional
    public FoodItem addFoodItem(FoodItemRequest request, String email) {

        Restaurant restaurant = getOwnedRestaurant(
                request.getRestaurantId(), email);

        FoodItem foodItem = new FoodItem();

        foodItem.setName(request.getName());
        foodItem.setDescription(request.getDescription());
        foodItem.setPrice(request.getPrice());
        foodItem.setAvailable(request.getAvailable());
        foodItem.setRestaurant(restaurant);

        return foodItemRepository.save(foodItem);
    }

    @Transactional
    public FoodItem updateFoodItem(
            Long foodItemId,
            FoodItemRequest request,
            String email) {

        FoodItem foodItem = getOwnedFoodItem(foodItemId, email);

        Restaurant restaurant = getOwnedRestaurant(
                request.getRestaurantId(), email);

        foodItem.setName(request.getName());
        foodItem.setDescription(request.getDescription());
        foodItem.setPrice(request.getPrice());
        foodItem.setAvailable(request.getAvailable());
        foodItem.setRestaurant(restaurant);

        return foodItemRepository.save(foodItem);
    }

    @Transactional
    public void deleteFoodItem(Long foodItemId, String email) {
        FoodItem foodItem = getOwnedFoodItem(foodItemId, email);

        cartItemRepository.deleteByFoodItemId(foodItemId);
        foodItemRepository.delete(foodItem);
    }

    public List<Restaurant> getMyRestaurants(String email) {
        return restaurantRepository.findByOwnerEmail(email);
    }

    public List<FoodItem> getMyMenu(String email) {
        return foodItemRepository.findByRestaurantOwnerEmail(email);
    }

    public List<Order> getMyOrders(String email) {
        return orderRepository
                .findByRestaurantOwnerEmailOrderByCreatedAtDesc(email);
    }

    @Transactional
    public Order updateOrderStatus(
            Long orderId,
            OrderStatus status,
            String email) {

        if (status == null) {
            throw new IllegalArgumentException("Order status is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getRestaurant() == null ||
                order.getRestaurant().getOwner() == null ||
                !order.getRestaurant().getOwner().getEmail()
                        .equalsIgnoreCase(email)) {

            throw new AccessDeniedException(
                    "You cannot update this restaurant's order");
        }

        order.setStatus(status);

        return orderRepository.save(order);
    }

    public FoodItem uploadFoodImage(
            Long foodItemId,
            MultipartFile file,
            String email) throws IOException {

        FoodItem foodItem = getOwnedFoodItem(foodItemId, email);

        String imageUrl = foodImageStorageService.saveImage(file);

        foodItem.setImageUrl(imageUrl);

        return foodItemRepository.save(foodItem);
    }
}