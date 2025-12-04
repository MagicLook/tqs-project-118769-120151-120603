package com.MagicLook.data;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID itemId;
    
    private String name;
    private String material;
    private String color;
    private String brand;
    private String size;

    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'AVAILABLE'")
    private String state;

    private BigDecimal priceRent;
    private BigDecimal priceSale;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "item_type_id")
    private ItemType itemType;

    // Constructors

    public Item() {

    }

    public Item(String name, String material, String color, String brand, String size,
                BigDecimal priceRent, BigDecimal priceSale, Shop shop, ItemType itemType) {
        this.name = name;
        this.material = material;
        this.color = color;
        this.brand = brand;
        this.size = size;
        this.priceRent = priceRent;
        this.priceSale = priceSale;
        this.shop = shop;
        this.itemType = itemType;
    }
}
