package com.MagicLook.data;

import jakarta.persistence.*;;

@Entity
@Table(name = "shop")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int shopId;

    private String name;
    private String location;

    // Constructors
    public Shop() {

    }

    public Shop(String name, String location) {
        this.name = name;
        this.location = location;
    }

    // Setters
    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }
}
