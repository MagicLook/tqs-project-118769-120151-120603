package com.MagicLook.data;

import jakarta.persistence.*;

@Entity
@Table(name = "item_type")
public class ItemType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String gender;
    private String category;

    // Constructors
    public ItemType() {

    }

    public ItemType(String gender, String category) {
        this.gender = gender;
        this.category = category;
    }

}
