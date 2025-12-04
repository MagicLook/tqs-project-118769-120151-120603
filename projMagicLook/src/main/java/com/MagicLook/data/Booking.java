package com.MagicLook.data;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.Date;

@Entity
@Table(name = "booking")
public class Booking  {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID bookingId;

    private Date bookingDate;

    private Date returnDate;

    private String state;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Constructors
    public Booking() {

    }

    public Booking(Date bookingDate, Date returnDate, String state, Item item) {
        this.bookingDate = bookingDate;
        this.returnDate = returnDate;
        this.state = state;
        this.item = item;
    }

}
