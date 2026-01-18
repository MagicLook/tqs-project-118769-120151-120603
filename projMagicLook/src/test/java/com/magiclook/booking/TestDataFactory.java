package com.magiclook.booking;

import com.magiclook.data.*;
import com.magiclook.dto.BookingRequestDTO;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class TestDataFactory {
    
    public static User createTestUser() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        // Only use setters that you know exist
        // Check your User class for available setters
        return user;
    }
    
    public static Item createTestItem() {
        Item item = new Item();
        item.setItemId(1);
        // Only use setters that you know exist
        // Check your Item class for available setters
        // If you have a price field, it might be named differently
        return item;
    }
    
    public static Booking createTestBooking(User user, Item item) {
        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID());
        booking.setUser(user);
        booking.setItem(item);
        booking.setState("CONFIRMED");
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        booking.setStartUseDate(startDate);
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();
        booking.setEndUseDate(endDate);
        
        booking.setCreatedAt(new Date());
        return booking;
    }
    
    public static BookingRequestDTO createBookingRequestDTO() {
        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setItemId(1);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        dto.setStartUseDate(cal.getTime());
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        dto.setEndUseDate(cal.getTime());
        
        return dto;
    }
}