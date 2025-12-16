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

    @ManyToOne
    @JoinColumn(name = "item_id")
    private transient Item item;

    public ItemSingle() {

    }

    public ItemSingle(String state, Item item) {
        this.state = state;
        this.item = item;
    }


    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

}
