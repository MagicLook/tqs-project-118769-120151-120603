package com.MagicLook.data;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.Date;
import java.io.Serializable;

@Entity
@Table(name = "booking")
public class Booking implements Serializable{
    private static final long serialVersionUID = 1L;

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

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
    
    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }
    
    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}