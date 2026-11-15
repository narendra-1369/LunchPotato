package com.lunchpotato.service;

import com.lunchpotato.entity.*;
import com.lunchpotato.repository.CartRepository;
import com.lunchpotato.repository.OrderRepository;
import com.lunchpotato.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order placeOrder(Long cartId, String email) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getUser() == null ||
                !cart.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException(
                    "You cannot place an order from another customer's cart");
        }

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty");
        }

        if (customer.getAddress() == null || customer.getAddress().isBlank()) {
            throw new IllegalArgumentException(
                    "Add a delivery address to your profile before ordering");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setDeliveryAddress(customer.getAddress());
        order.setStatus(OrderStatus.PLACED);

        Restaurant restaurant = cart.getItems()
                .get(0).getFoodItem().getRestaurant();

        order.setRestaurant(restaurant);

        double total = 0;

        for (CartItem cartItem : cart.getItems()) {

            OrderItem orderItem = new OrderItem();

            orderItem.setFoodName(cartItem.getFoodItem().getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setOrder(order);

            order.getItems().add(orderItem);

            total += cartItem.getUnitPrice() * cartItem.getQuantity();
        }

        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    public List<Order> getMyOrders(String email) {

        return orderRepository
                .findByCustomerEmailOrderByCreatedAtDesc(email);
    }

    public Order getCustomerOrder(Long orderId, String email) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getCustomer() == null ||
                !order.getCustomer().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException(
                    "You are not authorized to access this order");
        }

        return order;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status) {

        Order order = getOrderById(orderId);

        order.setStatus(status);

        return orderRepository.save(order);
    }
}