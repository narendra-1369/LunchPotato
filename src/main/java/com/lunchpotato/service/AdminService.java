package com.lunchpotato.service;

import com.lunchpotato.dto.AdminOverviewResponse;
import com.lunchpotato.dto.AdminRestaurantResponse;
import com.lunchpotato.entity.Restaurant;
import com.lunchpotato.entity.Role;
import com.lunchpotato.entity.User;
import com.lunchpotato.repository.RestaurantRepository;
import com.lunchpotato.repository.UserRepository;
import com.lunchpotato.repository.OrderRepository;
import com.lunchpotato.dto.RestaurantUpdateRequest;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final FoodImageStorageService imageStorage;

    public AdminService(
            RestaurantRepository restaurantRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            FoodImageStorageService imageStorage) {

        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.imageStorage = imageStorage;
    }

    @Transactional(readOnly = true)
    public AdminOverviewResponse getOverview() {
        return new AdminOverviewResponse(
                userRepository.count(),
                restaurantRepository.count(),
                orderRepository.count());
    }

    @Transactional(readOnly = true)
    public List<AdminRestaurantResponse> getAllRestaurants() {

        return restaurantRepository.findAll()
                .stream()
                .map(restaurant -> {

                    User owner = restaurant.getOwner();

                    return new AdminRestaurantResponse(
                            restaurant.getId(),
                            restaurant.getName(),
                            restaurant.getAddress(),
                            restaurant.getRating(),
                            restaurant.isOpen(),
                            owner != null ? owner.getFullName() : null,
                            owner != null ? owner.getEmail() : null,
                            restaurant.getLogoUrl());
                })
                .toList();
    }

    @Transactional
    public Restaurant assignOwner(Long restaurantId, String email) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        User owner = userRepository.findByEmail(
                email.trim().toLowerCase()).orElseThrow(() -> new RuntimeException("User not found"));

        if (owner.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException(
                    "An admin account cannot be assigned as a restaurant owner");
        }

        if (restaurant.getOwner() != null &&
                !restaurant.getOwner().getId().equals(owner.getId())) {
            throw new IllegalStateException(
                    "This restaurant is already assigned to another owner");
        }

        owner.setRole(Role.RESTAURANT_OWNER);
        restaurant.setOwner(owner);

        userRepository.save(owner);

        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public Restaurant updateRestaurant(
            Long id,
            RestaurantUpdateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        restaurant.setName(request.getName().trim());
        restaurant.setAddress(request.getAddress().trim());
        restaurant.setRating(request.getRating());
        restaurant.setOpen(request.isOpen());

        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public Restaurant uploadRestaurantLogo(
            Long id,
            MultipartFile file) throws IOException {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        restaurant.setLogoUrl(imageStorage.saveImage(file, "restaurants"));
        return restaurantRepository.save(restaurant);
    }
}