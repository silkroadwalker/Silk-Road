package com.circlemarketplace.model;

import java.util.List;

public class Ad {
    private Long id;
    private String title;
    private String description;
    private double price;
    private String city;
    private String category;
    private String status; // PENDING, ACTIVE, REJECTED, SOLD, DELETED
    private String ownerUsername;
    private List<String> imageUrls;
    private String createdAt;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCity() { return city; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getOwnerUsername() { return ownerUsername; }
    public List<String> getImageUrls() { return imageUrls; }
    public String getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setCity(String city) { this.city = city; }
    public void setCategory(String category) { this.category = category; }
    public void setStatus(String status) { this.status = status; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return title + " - " + price + " (" + city + ")";
    }
}
