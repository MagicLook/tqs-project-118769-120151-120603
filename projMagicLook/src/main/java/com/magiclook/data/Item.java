package com.magiclook.data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Builder.Default
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

    public Item(String name) {
        this.name = name;
    }
}