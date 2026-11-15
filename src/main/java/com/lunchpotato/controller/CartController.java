package com.lunchpotato.controller;

import com.lunchpotato.dto.CartItemRequest;
import com.lunchpotato.entity.Cart;
import com.lunchpotato.service.CartService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Create cart for logged-in customer
    @PostMapping
    public Cart createCart(Authentication authentication) {

        return cartService.createCart(authentication.getName());
    }

    // Add food item
    @PostMapping("/{cartId}/items")
    public Cart addItemToCart(
            @PathVariable Long cartId,
            @Valid @RequestBody CartItemRequest request,
            Authentication authentication) {

        return cartService.addItemToCart(
                cartId, request, authentication.getName());
    }

    // View cart
    @GetMapping("/{cartId}")
    public Cart getCart(
            @PathVariable Long cartId,
            Authentication authentication) {

        return cartService.getCart(
                cartId, authentication.getName());
    }

    // Update quantity
    @PutMapping("/{cartId}/items/{itemId}")
    public Cart updateItemQuantity(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @RequestParam int quantity,
            Authentication authentication) {

        return cartService.updateItemQuantity(
                cartId, itemId, quantity,
                authentication.getName());
    }

    // Remove item
    @DeleteMapping("/{cartId}/items/{itemId}")
    public Cart removeItem(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            Authentication authentication) {

        return cartService.removeItem(
                cartId, itemId, authentication.getName());
    }
}