package com.magiclook.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    public ItemDTO(String name) {
        this.name = name;
    }
}