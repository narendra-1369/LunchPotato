
package com.lunchpotato.repository;

import com.lunchpotato.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    // Find all food items belonging to a restaurant
    List<FoodItem> findByRestaurantId(Long restaurantId);

    List<FoodItem> findByNameContainingIgnoreCase(String name);

    List<FoodItem> findByAvailable(boolean available);

    List<FoodItem> findByRestaurantOwnerEmail(String email);

}
