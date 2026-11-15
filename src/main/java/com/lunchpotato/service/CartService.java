package com.lunchpotato.service;

import com.lunchpotato.dto.CartItemRequest;
import com.lunchpotato.entity.*;
import com.lunchpotato.repository.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            FoodItemRepository foodItemRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.foodItemRepository = foodItemRepository;
        this.userRepository = userRepository;
    }

    // Create cart for the logged-in customer
    public Cart createCart(String email) {

        User user = userRepository.findByEmail(
                email.trim().toLowerCase()).orElseThrow(() -> new RuntimeException("Customer not found"));

        Cart cart = new Cart();
        cart.setUser(user);

        return cartRepository.save(cart);
    }

    // Find cart and verify ownership
    private Cart getOwnedCart(Long cartId, String email) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getUser() == null ||
                !cart.getUser().getEmail().equalsIgnoreCase(email)) {

            throw new AccessDeniedException(
                    "You are not authorized to access this cart");
        }

        return cart;
    }

    // View customer's cart
    @Transactional(readOnly = true)
    public Cart getCart(Long cartId, String email) {
        return getOwnedCart(cartId, email);
    }

    // Add food item
    @Transactional
    public Cart addItemToCart(
            Long cartId,
            CartItemRequest request,
            String email) {

        Cart cart = getOwnedCart(cartId, email);

        var foodItem = foodItemRepository.findById(
                request.getFoodItemId()).orElseThrow(() -> new RuntimeException("Food item not found"));

        if (!foodItem.isAvailable()) {
            throw new RuntimeException("Food item is not available");
        }

        if (!cart.getItems().isEmpty()) {

            Long existingRestaurantId = cart.getItems()
                    .get(0).getFoodItem().getRestaurant().getId();

            Long newRestaurantId = foodItem.getRestaurant().getId();

            if (!existingRestaurantId.equals(newRestaurantId)) {
                throw new IllegalArgumentException(
                        "You can order from only one restaurant per cart");
            }
        }

        var existingItem = cartItemRepository
                .findByCartIdAndFoodItemId(
                        cartId, foodItem.getId());

        if (existingItem.isPresent()) {

            CartItem cartItem = existingItem.get();

            cartItem.setQuantity(
                    cartItem.getQuantity() + request.getQuantity());

        } else {

            CartItem cartItem = new CartItem();

            cartItem.setCart(cart);
            cartItem.setFoodItem(foodItem);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUnitPrice(foodItem.getPrice());

            cart.getItems().add(cartItem);
        }

        return cartRepository.save(cart);
    }

    // Update quantity
    @Transactional
    public Cart updateItemQuantity(
            Long cartId,
            Long itemId,
            int quantity,
            String email) {

        if (quantity < 1) {
            throw new RuntimeException(
                    "Quantity must be at least 1");
        }

        Cart cart = getOwnedCart(cartId, email);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getId().equals(cartId)) {
            throw new RuntimeException(
                    "Item does not belong to this cart");
        }

        item.setQuantity(quantity);

        return cartRepository.save(cart);
    }

    // Remove item
    @Transactional
    public Cart removeItem(
            Long cartId,
            Long itemId,
            String email) {

        Cart cart = getOwnedCart(cartId, email);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getId().equals(cartId)) {
            throw new RuntimeException(
                    "Item does not belong to this cart");
        }

        cart.getItems().remove(item);

        return cartRepository.save(cart);
    }
}