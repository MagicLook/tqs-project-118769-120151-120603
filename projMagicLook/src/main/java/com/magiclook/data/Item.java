package com.magiclook.data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "item")
public class Item implements Serializable{
    private static final Long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;
    
    private String name;
    private String brand;
    private String material;
    private String color;

    private BigDecimal priceRent;
    private BigDecimal priceSale;

    @Column(length = 500)
    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "item_type_id")
    private ItemType itemType;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<ItemSingle> itemSingles;

    public Item() {}

    public Item(String name, String material, String color, String brand,
                BigDecimal priceRent, BigDecimal priceSale, Shop shop, ItemType itemType) {
        this.name = name;
        this.material = material;
        this.color = color;
        this.brand = brand;
        this.priceRent = priceRent;
        this.priceSale = priceSale;
        this.shop = shop;
        this.itemType = itemType;
    }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public BigDecimal getPriceRent() { return priceRent; }
    public void setPriceRent(BigDecimal priceRent) { this.priceRent = priceRent; }
    
    public BigDecimal getPriceSale() { return priceSale; }
    public void setPriceSale(BigDecimal priceSale) { this.priceSale = priceSale; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }
    
    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }

    public List<ItemSingle> getItemSingles() { return itemSingles; }
    public void setItemSingles(List<ItemSingle> itemSingles) { this.itemSingles = itemSingles; }

    @Transient
    public boolean isAvailable() {
        if (itemSingles == null) return false;
        return itemSingles.stream()
            .anyMatch(is -> "AVAILABLE".equals(is.getState()));
    }
}