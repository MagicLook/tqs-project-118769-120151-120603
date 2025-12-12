package com.MagicLook.data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "item")
// É um grupo de items
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;
    
    private String name;
    private String material;
    private String color;
    private String brand;
    private String size;

    private BigDecimal priceRent;
    private BigDecimal priceSale;

    // Caminho da imagem
    @Column(length = 500)
    private String imagePath; 

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "item_type_id")
    private ItemType itemType;

    @OneToMany
    @JoinColumn(name = "item_single_id")
    private List<ItemSingle> itemSigle;

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

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public List<ItemSingle> getItemSigle() { return itemSigle; }
    public void setItemSigle(List<ItemSingle> itemSigle) { this.itemSigle = itemSigle; }

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
}