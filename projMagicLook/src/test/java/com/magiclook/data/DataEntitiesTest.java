package com.magiclook.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DataEntitiesTest {
    
    private Item item;
    private Shop shop;
    private ItemType itemType;
    private User user;
    private Booking booking;
    
    @BeforeEach
    void setUp() {
        // Create shop
        shop = new Shop("Test Shop", "Test Location");
        shop.setShopId(1);
        
        // Create item type
        itemType = new ItemType("M", "Clothing", "Shirt");
        itemType.setId(1);
        
        // Create item
        item = new Item(
            "Test Shirt",
            "Cotton",
            "Blue",
            "Test Brand",
            new BigDecimal("19.99"),
            new BigDecimal("99.99"),
            shop,
            itemType
        );
        item.setItemId(1);
        
        // Create user
        user = new User(
            "John",
            "Doe",
            "john@test.com",
            "123456789",
            "password123",
            "johndoe"
        );
        user.setUserId(UUID.randomUUID());
        
        // Create booking dates
        Date pickupDate = new Date(System.currentTimeMillis() + 86400000); // Tomorrow
        Date startUseDate = new Date(System.currentTimeMillis() + 172800000); // Day after tomorrow
        Date endUseDate = new Date(System.currentTimeMillis() + 259200000); // 3 days from now
        Date returnDate = new Date(System.currentTimeMillis() + 345600000); // 4 days from now
        
        // Create booking
        booking = new Booking();
        booking.setBookingId(UUID.randomUUID());
        booking.setPickupDate(pickupDate);
        booking.setStartUseDate(startUseDate);
        booking.setEndUseDate(endUseDate);
        booking.setReturnDate(returnDate);
        booking.setTotalDays(2);
        booking.setTotalPrice(new BigDecimal("39.98"));
        booking.setState("CONFIRMED");
        booking.setItem(item);
        booking.setUser(user);
    }
    
    @Test
    void testBookingCreation() {
        assertNotNull(booking);
        assertNotNull(booking.getBookingId());
        assertEquals("CONFIRMED", booking.getState());
        assertEquals(item, booking.getItem());
        assertEquals(user, booking.getUser());
        assertEquals(2, booking.getTotalDays());
        assertEquals(new BigDecimal("39.98"), booking.getTotalPrice());
    }
    
    @Test
    void testBookingDates() {
        assertNotNull(booking.getPickupDate());
        assertNotNull(booking.getStartUseDate());
        assertNotNull(booking.getEndUseDate());
        assertNotNull(booking.getReturnDate());
        
        // Verify dates are in correct order
        assertTrue(booking.getPickupDate().before(booking.getStartUseDate()));
        assertTrue(booking.getStartUseDate().before(booking.getEndUseDate()));
        assertTrue(booking.getEndUseDate().before(booking.getReturnDate()));
    }
    
    @Test
    void testCalculateUseDays() {
        // Create dates with known difference
        Date start = new Date();
        Date end = new Date(start.getTime() + (3 * 86400000)); // 3 days later
        
        booking.setStartUseDate(start);
        booking.setEndUseDate(end);
        
        // Calculate difference in days
        long diff = end.getTime() - start.getTime();
        long expectedDays = (diff / (1000 * 60 * 60 * 24)) + 1;
        
        assertEquals(expectedDays, booking.calculateUseDays());
    }
    
    @Test
    void testItemCreation() {
        assertNotNull(item);
        assertEquals("Test Shirt", item.getName());
        assertEquals("Cotton", item.getMaterial());
        assertEquals("Blue", item.getColor());
        assertEquals("Test Brand", item.getBrand());
        assertEquals(new BigDecimal("19.99"), item.getPriceRent());
        assertEquals(new BigDecimal("99.99"), item.getPriceSale());
        assertEquals(shop, item.getShop());
        assertEquals(itemType, item.getItemType());
    }
    
    @Test
    void testItemAvailability() {
        // Initially available
        assertTrue(item.isAvailable());
        
        // Set as unavailable
        item.setAvailable(false);
        assertFalse(item.isAvailable());
        
        // Set next available date
        Date nextDate = new Date();
        item.setNextAvailableDate(nextDate);
        assertEquals(nextDate, item.getNextAvailableDate());
    }
    
    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@test.com", user.getEmail());
        assertEquals("123456789", user.getTelephone());
        assertEquals("johndoe", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("John Doe", user.getFullName());
    }
    
    @Test
    void testShopCreation() {
        assertNotNull(shop);
        assertEquals("Test Shop", shop.getName());
        assertEquals("Test Location", shop.getLocation());
        assertEquals(1, shop.getShopId());
    }
    
    @Test
    void testItemTypeCreation() {
        assertNotNull(itemType);
        assertEquals("M", itemType.getGender());
        assertEquals("Clothing", itemType.getCategory());
        assertEquals("Shirt", itemType.getSubcategory());
        assertEquals(1, itemType.getId());
    }
    
    @Test
    void testBookingConstructor() {
        Date pickupDate = new Date();
        Date startUseDate = new Date(pickupDate.getTime() + 86400000);
        Date endUseDate = new Date(startUseDate.getTime() + 86400000);
        Date returnDate = new Date(endUseDate.getTime() + 86400000);
        
        Booking newBooking = new Booking(
            pickupDate,
            startUseDate,
            endUseDate,
            returnDate,
            "PENDING",
            item,
            user
        );
        
        assertNotNull(newBooking);
        assertEquals(pickupDate, newBooking.getPickupDate());
        assertEquals(startUseDate, newBooking.getStartUseDate());
        assertEquals(endUseDate, newBooking.getEndUseDate());
        assertEquals(returnDate, newBooking.getReturnDate());
        assertEquals("PENDING", newBooking.getState());
        assertEquals(item, newBooking.getItem());
        assertEquals(user, newBooking.getUser());
        assertNotNull(newBooking.getCreatedAt());
    }
}