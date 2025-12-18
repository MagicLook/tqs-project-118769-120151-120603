package com.magiclook.data;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.Date;
import java.math.BigDecimal;
import java.io.Serializable;

@Entity
@Table(name = "booking")
public class Booking implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID bookingId;

    @Column(name = "pickup_date")
    private Date pickupDate;

    @Column(name = "start_use_date")
    private Date startUseDate;

    @Column(name = "end_use_date")
    private Date endUseDate;

    @Column(name = "return_date")
    private Date returnDate;

    @Column(name = "total_days")
    private int totalDays;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    private String state;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    // Constructors
    public Booking() {}

    public Booking(Date pickupDate, Date startUseDate, Date endUseDate, 
                   Date returnDate, String state, Item item, User user) {
        this.pickupDate = pickupDate;
        this.startUseDate = startUseDate;
        this.endUseDate = endUseDate;
        this.returnDate = returnDate;
        this.state = state;
        this.item = item;
        this.user = user;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
    
    public Date getPickupDate() { return pickupDate; }
    public void setPickupDate(Date pickupDate) { this.pickupDate = pickupDate; }
    
    public Date getStartUseDate() { return startUseDate; }
    public void setStartUseDate(Date startUseDate) { this.startUseDate = startUseDate; }
    
    public Date getEndUseDate() { return endUseDate; }
    public void setEndUseDate(Date endUseDate) { this.endUseDate = endUseDate; }
    
    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }
    
    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // Helper method to calculate total days of use
    public long calculateUseDays() {
        if (startUseDate == null || endUseDate == null) return 0;
        long diff = endUseDate.getTime() - startUseDate.getTime();
        return (diff / (1000 * 60 * 60 * 24)) + 1;
    }
    
    public String getCurrentState() {
    Date now = new Date();
    if (state.equals("CANCELLED") || state.equals("COMPLETED")) {
        return state;
    }
    
    if (now.before(startUseDate)) {
        return "CONFIRMED";
    } else if (now.after(endUseDate)) {
        // Check if past return date without returning
        if (now.after(returnDate) && !state.equals("RETURNED")) {
            return "OVERDUE";
        } else {
            return "COMPLETED";
        }
    } else {
        return "ACTIVE";
    }
}
}