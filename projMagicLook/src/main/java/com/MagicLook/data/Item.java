package com.MagicLook.data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import java.io.Serializable;

@Entity
@Table(name = "item")
public class Item implements Serializable{
    private static final long serialVersionUID = 1L;
    
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

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public BigDecimal getPriceRent() { return priceRent; }
    public void setPriceRent(BigDecimal priceRent) { this.priceRent = priceRent; }
    
    public BigDecimal getPriceSale() { return priceSale; }
    public void setPriceSale(BigDecimal priceSale) { this.priceSale = priceSale; }
    
    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }
    
    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }
}