
package com.lunchpotato.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Restaurant name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Restaurant address is required")
    @Column(nullable = false)
    private String address;

    @DecimalMin(value = "0.0", message = "Rating cannot be below 0")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5")
    private Double rating;

    @Column(name = "is_open")
    private boolean open;

    @Column(name = "logo_url")
    private String logoUrl;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    public Restaurant() {
    }

    public Restaurant(String name, String address,
            Double rating, boolean open) {
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.open = open;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
