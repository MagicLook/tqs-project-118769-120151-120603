package com.magiclook.dto;

public class ItemFilterDTO {
    private String color;
    private String brand;
    private String material;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private String shopLocation; // Nova propriedade
    
    public ItemFilterDTO() {}
    
    public ItemFilterDTO(String color, String brand, String material, String category, 
                        Double minPrice, Double maxPrice) {
        this.color = color;
        this.brand = brand;
        this.material = material;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.shopLocation = null;
    }

    public ItemFilterDTO(String color, String brand, String material, String category, 
                        Double minPrice, Double maxPrice, String shopLocation) {
        this.color = color;
        this.brand = brand;
        this.material = material;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.shopLocation = shopLocation;
    }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }
    
    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }
    
    public String getShopLocation() { return shopLocation; }
    public void setShopLocation(String shopLocation) { this.shopLocation = shopLocation; }
    
    public boolean hasFilters() {
        return (color != null && !color.isEmpty()) ||
               (brand != null && !brand.isEmpty()) ||
               (material != null && !material.isEmpty()) ||
               (category != null && !category.isEmpty()) ||
               (shopLocation != null && !shopLocation.isEmpty()) ||
               minPrice != null ||
               maxPrice != null;
    }
}