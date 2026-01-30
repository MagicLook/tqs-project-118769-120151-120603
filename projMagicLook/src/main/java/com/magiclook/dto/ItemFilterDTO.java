package com.magiclook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemFilterDTO {
    private String color;
    private String brand;
    private String material;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private String shopLocation;
    private String subcategory;
    private String size;

    public boolean hasFilters() {
        return (color != null && !color.isEmpty()) ||
                (brand != null && !brand.isEmpty()) ||
                (material != null && !material.isEmpty()) ||
                (category != null && !category.isEmpty()) ||
                (subcategory != null && !subcategory.isEmpty()) ||
                (shopLocation != null && !shopLocation.isEmpty()) ||
                (size != null && !size.isEmpty()) ||
                minPrice != null ||
                maxPrice != null;
    }
}