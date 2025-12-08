package com.MagicLook.data;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "item_type")
public class ItemType implements Serializable{
    private static final long serialVersionUID = 1L;

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

    // Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}