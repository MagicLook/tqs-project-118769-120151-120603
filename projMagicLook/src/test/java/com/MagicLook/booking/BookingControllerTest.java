package com.magiclook.booking;

import com.magiclook.boundary.BookingController;
import com.magiclook.data.*;
import com.magiclook.service.BookingService;
import com.magiclook.service.ItemService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {
    
    @Mock
    private BookingService bookingService;
    
    @Mock
    private ItemService itemService;
    
    @Mock
    private HttpSession session;
    
    @Mock
    private Model model;
    
    @InjectMocks
    private BookingController bookingController;
    
    private User testUser;
    private Item testItem;
    private Booking testBooking;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setUsername("testuser");
        
        testItem = new Item();
        testItem.setItemId(1);
        testItem.setName("Test Item");
        testItem.setPriceRent(new BigDecimal("25.00"));
        
        testBooking = new Booking();
        testBooking.setBookingId(UUID.randomUUID());
        testBooking.setItem(testItem);
        testBooking.setUser(testUser);
    }
    
    @Test
    void testShowBookingForm_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(1)).thenReturn(testItem);
        
        String view = bookingController.showBookingForm(1, session, model);
        
        assertEquals("booking/bookingForm", view);
        verify(model).addAttribute(eq("item"), eq(testItem));
        verify(model).addAttribute(eq("user"), eq(testUser));
    }
    
    @Test
    void testShowBookingForm_UserNotLoggedIn() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);
        
        String view = bookingController.showBookingForm(1, session, model);
        
        assertEquals("redirect:/magiclook/login", view);
        verify(session).setAttribute(eq("redirectAfterLogin"), anyString());
    }
    
    @Test
    void testCreateBooking_Success() throws Exception {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(1)).thenReturn(testItem);
        when(bookingService.checkAvailability(anyInt(), any(), any())).thenReturn(true);
        when(bookingService.createBooking(any(), eq(testUser))).thenReturn(testBooking);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();
        
        String view = bookingController.createBooking(1, startDate, endDate, session, model);
        
        assertTrue(view.contains("redirect:/magiclook/booking/confirmation/"));
    }
    
    @Test
    void testCreateBooking_ItemNotAvailable() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(1)).thenReturn(testItem);
        when(bookingService.checkAvailability(anyInt(), any(), any())).thenReturn(false);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();
        
        String view = bookingController.createBooking(1, startDate, endDate, session, model);
        
        assertEquals("booking/bookingForm", view);
        verify(model).addAttribute(eq("error"), anyString());
        verify(model).addAttribute(eq("item"), eq(testItem));
    }
    
    @Test
    void testShowConfirmation_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);
        
        String view = bookingController.showConfirmation(testBooking.getBookingId(), session, model);
        
        assertEquals("booking/bookingConfirmation", view);
        verify(model).addAttribute(eq("booking"), eq(testBooking));
        verify(model).addAttribute(eq("user"), eq(testUser));
    }
    
    @Test
    void testShowMyBookings_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);
        
        String view = bookingController.showMyBookings(session, model);
        
        assertEquals("booking/myBookings", view);
        verify(model).addAttribute(eq("bookings"), eq(bookings));
        verify(model).addAttribute(eq("user"), eq(testUser));
        verify(model).addAttribute("activePage", "myBookings");
    }
}