package com.magiclook.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public class ItemDTO {

    private Integer itemId;
    
    private String name;
    private String material;
    private String color;
    private String brand;
    private String size;

    private String state;

    private BigDecimal priceRent;
    private BigDecimal priceSale;

    @NotNull
    private Integer shopId;

    @NotNull
    private String gender;

    @NotNull
    private String category;
    
    @NotNull
    private String subcategory;
    
    private String imagePath;

    public ItemDTO() {

    }

    public ItemDTO(String name, String material, String color, String brand, String size,
                BigDecimal priceRent, BigDecimal priceSale, Integer shopId, String gender, String category, String subcategory) {
        
        this.name = name;
        this.material = material;
        this.color = color;
        this.brand = brand;
        this.size = size;
        this.priceRent = priceRent;
        this.priceSale = priceSale;
        this.shopId = shopId;
        this.gender = gender;
        this.category = category;
        this.subcategory = subcategory;
    }

    // Getters
    
    public Integer getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getMaterial() {
        return material;
    }

    public String getColor() {
        return color;
    }

    public String getBrand() {
        return brand;
    }

    public String getSize() {
        return size;
    }

    public String getState() {
        return state;
    }

    public BigDecimal getPriceRent() {
        return priceRent;
    }

    public BigDecimal getPriceSale() {
        return priceSale;
    }

    public Integer getShopId() {
        return shopId;
    }

    public String getGender() {
        return gender;
    }

    public String getCategory() {
        return category;
    }

    public String getSubcategory() {
        return subcategory;
    }
    

    public String getImagePath() {
        return imagePath;
    }

    // Setters
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setPriceRent(BigDecimal priceRent) {
        this.priceRent = priceRent;
    }

    public void setPriceSale(BigDecimal priceSale) {
        this.priceSale = priceSale;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}