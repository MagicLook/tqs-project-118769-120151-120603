package com.MagicLook.data;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "shop")
public class Shop implements Serializable{
    private static final long serialVersionUID = 1L;

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

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }
}