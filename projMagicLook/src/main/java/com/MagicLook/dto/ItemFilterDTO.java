package com.MagicLook.dto;

public class ItemFilterDTO {
    private String color;
    private String brand;
    private String material;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    
    public ItemFilterDTO() {}
    
    public ItemFilterDTO(String color, String brand, String material, String category, 
                        Double minPrice, Double maxPrice) {
        this.color = color;
        this.brand = brand;
        this.material = material;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
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
    
    public boolean hasFilters() {
        return (color != null && !color.isEmpty()) ||
               (brand != null && !brand.isEmpty()) ||
               (material != null && !material.isEmpty()) ||
               (category != null && !category.isEmpty()) ||
               minPrice != null ||
               maxPrice != null;
    }
}