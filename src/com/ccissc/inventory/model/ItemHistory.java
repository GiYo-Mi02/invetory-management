package com.ccissc.inventory.model;

import java.time.LocalDateTime;

public class ItemHistory {
    private int id;
    private int itemId;
    private String action;
    private int changedBy;
    private String changedByName;
    private String oldName;
    private String newName;
    private String oldDescription;
    private String newDescription;
    private Integer oldQuantity;
    private Integer newQuantity;
    private Integer oldCategoryId;
    private Integer newCategoryId;
    private Integer oldMinQuantity;
    private Integer newMinQuantity;
    private Boolean oldArchived;
    private Boolean newArchived;
    private String note;
    private LocalDateTime createdAt;

    public ItemHistory() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(int changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getOldDescription() {
        return oldDescription;
    }

    public void setOldDescription(String oldDescription) {
        this.oldDescription = oldDescription;
    }

    public String getNewDescription() {
        return newDescription;
    }

    public void setNewDescription(String newDescription) {
        this.newDescription = newDescription;
    }

    public Integer getOldQuantity() {
        return oldQuantity;
    }

    public void setOldQuantity(Integer oldQuantity) {
        this.oldQuantity = oldQuantity;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public Integer getOldCategoryId() {
        return oldCategoryId;
    }

    public void setOldCategoryId(Integer oldCategoryId) {
        this.oldCategoryId = oldCategoryId;
    }

    public Integer getNewCategoryId() {
        return newCategoryId;
    }

    public void setNewCategoryId(Integer newCategoryId) {
        this.newCategoryId = newCategoryId;
    }

    public Integer getOldMinQuantity() {
        return oldMinQuantity;
    }

    public void setOldMinQuantity(Integer oldMinQuantity) {
        this.oldMinQuantity = oldMinQuantity;
    }

    public Integer getNewMinQuantity() {
        return newMinQuantity;
    }

    public void setNewMinQuantity(Integer newMinQuantity) {
        this.newMinQuantity = newMinQuantity;
    }

    public Boolean getOldArchived() {
        return oldArchived;
    }

    public void setOldArchived(Boolean oldArchived) {
        this.oldArchived = oldArchived;
    }

    public Boolean getNewArchived() {
        return newArchived;
    }

    public void setNewArchived(Boolean newArchived) {
        this.newArchived = newArchived;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
