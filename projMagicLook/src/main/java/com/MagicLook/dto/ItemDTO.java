package com.MagicLook.dto;

import com.MagicLook.data.*;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.*;

public class ItemDTO {

    private UUID itemId;
    
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


    // Constructors
    // Needs this??
    public ItemDTO() {

    }

    public ItemDTO(String name, String material, String color, String brand, String size,
                BigDecimal priceRent, BigDecimal priceSale, Integer shopId, String gender, String category) {
        
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
    }

    // Getters
    
    public UUID getItemId() {
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
}