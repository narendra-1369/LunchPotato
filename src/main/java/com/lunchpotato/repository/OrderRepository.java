package com.lunchpotato.repository;

import com.lunchpotato.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String email);

    List<Order> findByRestaurantOwnerEmailOrderByCreatedAtDesc(String email);

}