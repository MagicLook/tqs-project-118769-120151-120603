package com.magiclook.data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "item")
public class Item implements Serializable {

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

    @Column(name = "next_available_date")
    private Date nextAvailableDate;

    @Column(name = "is_available")
    private boolean available = true;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "item_type_id")
    private ItemType itemType;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<ItemSingle> itemSingles;

    public Item() {
    }

    public Item(String name) {
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
        private Shop shop;
        private ItemType itemType;

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

        public Builder shop(Shop shop) {
            this.shop = shop;
            return this;
        }

        public Builder itemType(ItemType itemType) {
            this.itemType = itemType;
            return this;
        }

        public Item build() {
            Item item = new Item();
            item.setName(name);
            item.setMaterial(material);
            item.setColor(color);
            item.setBrand(brand);
            item.setPriceRent(priceRent);
            item.setPriceSale(priceSale);
            item.setShop(shop);
            item.setItemType(itemType);
            return item;
        }
    }

    // Getters and setters
    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigDecimal getPriceRent() {
        return priceRent;
    }

    public void setPriceRent(BigDecimal priceRent) {
        this.priceRent = priceRent;
    }

    public BigDecimal getPriceSale() {
        return priceSale;
    }

    public void setPriceSale(BigDecimal priceSale) {
        this.priceSale = priceSale;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public List<ItemSingle> getItemSingles() {
        return itemSingles;
    }

    public void setItemSingles(List<ItemSingle> itemSingles) {
        this.itemSingles = itemSingles;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Date getNextAvailableDate() {
        return nextAvailableDate;
    }

    public void setNextAvailableDate(Date nextAvailableDate) {
        this.nextAvailableDate = nextAvailableDate;
    }
}