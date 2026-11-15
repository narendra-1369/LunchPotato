package com.lunchpotato.dto;

public record AdminRestaurantResponse(
        Long id,
        String name,
        String address,
        Double rating,
        boolean open,
        String ownerName,
        String ownerEmail,
        String logoUrl) {
}