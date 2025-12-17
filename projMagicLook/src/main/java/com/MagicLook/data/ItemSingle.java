package com.magiclook.data;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "item_single")
public class ItemSingle implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'AVAILABLE'")
    private String state; // Estado atual

    private String size; // Tamanho do item

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    public ItemSingle() {

    }

    public ItemSingle(String state, Item item, String size) {
        this.state = state;
        this.item = item;
        this.size = size;
    }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
}
