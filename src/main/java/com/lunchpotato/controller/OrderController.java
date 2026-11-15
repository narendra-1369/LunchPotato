package com.lunchpotato.controller;

import com.lunchpotato.entity.Order;
import com.lunchpotato.entity.OrderStatus;
import com.lunchpotato.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place/{cartId}")
    public ResponseEntity<Order> placeOrder(
            @PathVariable Long cartId,
            Authentication authentication) {

        Order order = orderService.placeOrder(
                cartId,
                authentication.getName());

        return ResponseEntity.ok(order);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(
            Authentication authentication) {

        return ResponseEntity.ok(
                orderService.getMyOrders(authentication.getName()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        return ResponseEntity.ok(
                orderService.getCustomerOrder(
                        orderId,
                        authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {

        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(orderId, status));
    }
}