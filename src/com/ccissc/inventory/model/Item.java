package com.ccissc.inventory.model;

import java.time.LocalDateTime;

public class Item {
    private int id;
    private String itemName;
    private String description;
    private int quantity;
    private int minQuantity;
    private String imagePath;
    private int categoryId;
    private String categoryName;
    private boolean archived;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Item() {
    }

    public Item(int id, String itemName, String description, int quantity, int minQuantity, String imagePath,
            int categoryId, String categoryName, boolean archived, int createdBy, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.quantity = quantity;
        this.minQuantity = minQuantity;
        this.imagePath = imagePath;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.archived = archived;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
