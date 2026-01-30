package com.magiclook.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public class ItemDTO {

    private Integer itemId;

    private String name;
    private String material;
    private String color;
    private String brand;

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

    public ItemDTO(String name) {
        this.name = name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String material;
        private String color;
        private String brand;
        private BigDecimal priceRent;
        private BigDecimal priceSale;
        private Integer shopId;
        private String gender;
        private String category;
        private String subcategory;
        private String imagePath;
        private String state;
        private Integer itemId;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder material(String material) {
            this.material = material;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder priceRent(BigDecimal priceRent) {
            this.priceRent = priceRent;
            return this;
        }

        public Builder priceSale(BigDecimal priceSale) {
            this.priceSale = priceSale;
            return this;
        }

        public Builder shopId(Integer shopId) {
            this.shopId = shopId;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder subcategory(String subcategory) {
            this.subcategory = subcategory;
            return this;
        }

        public Builder imagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder itemId(Integer itemId) {
            this.itemId = itemId;
            return this;
        }

        public ItemDTO build() {
            ItemDTO dto = new ItemDTO();
            dto.setName(name);
            dto.setMaterial(material);
            dto.setColor(color);
            dto.setBrand(brand);
            dto.setPriceRent(priceRent);
            dto.setPriceSale(priceSale);
            dto.setShopId(shopId);
            dto.setGender(gender);
            dto.setCategory(category);
            dto.setSubcategory(subcategory);
            dto.setImagePath(imagePath);
            dto.setState(state);
            dto.setItemId(itemId);
            return dto;
        }
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