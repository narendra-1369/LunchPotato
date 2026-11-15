package com.lunchpotato.dto;

public record AdminOverviewResponse(
        long totalUsers,
        long totalRestaurants,
        long totalOrders) {
}