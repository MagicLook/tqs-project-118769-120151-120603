package com.MagicLook.data;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AllDataEntitiesTest {

    @Test
    void testAllEntities() {
        // Testar User
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setUserId(userId);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@test.com");
        user.setUsername("john");
        user.setPassword("pass123");
        user.setTelephone("912345678");

        assertEquals(userId, user.getUserId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@test.com", user.getEmail());
        assertEquals("john", user.getUsername());
        assertEquals("pass123", user.getPassword());
        assertEquals("912345678", user.getTelephone());
        assertEquals("John Doe", user.getFullName());

        // Testar User com construtor
        User user2 = new User("Jane", "Smith", "jane@test.com", "987654321", "pass456", "jane");
        assertEquals("Jane", user2.getFirstName());
        assertEquals("Smith", user2.getLastName());
        assertEquals("Jane Smith", user2.getFullName());

        // Testar Staff
        Shop shop = new Shop();
        shop.setShopId(1);
        shop.setName("Loja Teste");
        shop.setLocation("Localização");

        Staff staff = new Staff();
        UUID staffId = UUID.randomUUID();
        staff.setStaffId(staffId);
        staff.setName("Staff Name");
        staff.setEmail("staff@test.com");
        staff.setUsername("staffuser");
        staff.setPassword("pass123");
        staff.setShop(shop);
        staff.setRole("ADMIN");

        assertEquals(staffId, staff.getStaffId());
        assertEquals("Staff Name", staff.getName());
        assertEquals("staff@test.com", staff.getEmail());
        assertEquals("staffuser", staff.getUsername());
        assertEquals("pass123", staff.getPassword());
        assertEquals(shop, staff.getShop());
        assertEquals("ADMIN", staff.getRole());

        // Testar Shop
        assertEquals(1, shop.getShopId());
        assertEquals("Loja Teste", shop.getName());
        assertEquals("Localização", shop.getLocation());

        Shop shop2 = new Shop("Nova Loja", "Nova Localização");
        assertEquals("Nova Loja", shop2.getName());
        assertEquals("Nova Localização", shop2.getLocation());

        // Testar ItemType
        ItemType itemType = new ItemType();
        itemType.setId(1);
        itemType.setGender("M");
        itemType.setCategory("Camiseta");

        assertEquals(1, itemType.getId());
        assertEquals("M", itemType.getGender());
        assertEquals("Camiseta", itemType.getCategory());

        ItemType itemType2 = new ItemType("F", "Vestido", "Curto");
        assertEquals("F", itemType2.getGender());
        assertEquals("Vestido", itemType2.getCategory());

        // Testar Item
        Item item = new Item();
        UUID itemId = UUID.randomUUID();
        item.setItemId(1);
        item.setName("Camiseta");
        item.setMaterial("Algodão");
        item.setColor("Azul");
        item.setBrand("Marca");
        item.setSize("M");
        item.setPriceRent(new BigDecimal("10.00"));
        item.setPriceSale(new BigDecimal("50.00"));
        item.setShop(shop);
        item.setItemType(itemType);

        assertEquals(itemId, item.getItemId());
        assertEquals("Camiseta", item.getName());
        assertEquals("Algodão", item.getMaterial());
        assertEquals("Azul", item.getColor());
        assertEquals("Marca", item.getBrand());
        assertEquals("M", item.getSize());
        assertEquals(new BigDecimal("10.00"), item.getPriceRent());
        assertEquals(new BigDecimal("50.00"), item.getPriceSale());
        assertEquals(shop, item.getShop());
        assertEquals(itemType, item.getItemType());

        // Testar Booking
        Booking booking = new Booking();
        UUID bookingId = UUID.randomUUID();
        Date now = new Date();
        booking.setBookingId(bookingId);
        booking.setBookingDate(now);
        booking.setReturnDate(now);
        booking.setState("ACTIVE");
        booking.setItem(item);
        booking.setUser(user);

        assertEquals(bookingId, booking.getBookingId());
        assertEquals(now, booking.getBookingDate());
        assertEquals(now, booking.getReturnDate());
        assertEquals("ACTIVE", booking.getState());
        assertEquals(item, booking.getItem());
        assertEquals(user, booking.getUser());

        // Testar Booking com construtor
        Booking booking2 = new Booking(now, now, "PENDING", item);
        assertEquals(now, booking2.getBookingDate());
        assertEquals(now, booking2.getReturnDate());
        assertEquals("PENDING", booking2.getState());
        assertEquals(item, booking2.getItem());
    }
}