package com.lunchpotato.repository;

import java.util.Optional;
import com.lunchpotato.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndFoodItemId(
            Long cartId, Long foodItemId);

    void deleteByFoodItemId(Long foodItemId);
}