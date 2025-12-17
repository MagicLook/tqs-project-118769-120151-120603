package com.magiclook.booking;

import com.magiclook.data.*;
import com.magiclook.dto.BookingRequestDTO;
import com.magiclook.repository.BookingRepository;
import com.magiclook.repository.ItemRepository;
import com.magiclook.repository.UserRepository;
import com.magiclook.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    
    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private ItemRepository itemRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private BookingService bookingService;
    
    private User testUser;
    private Item testItem;
    private BookingRequestDTO bookingRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setUsername("testuser");
        
        testItem = new Item();
        testItem.setItemId(1);
        testItem.setName("Test Item");
        testItem.setPriceRent(new BigDecimal("25.00"));
        testItem.setAvailable(true);
        
        bookingRequest = new BookingRequestDTO();
        bookingRequest.setItemId(1);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        bookingRequest.setStartUseDate(cal.getTime());
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        bookingRequest.setEndUseDate(cal.getTime());
    }
    
    @Test
    void testCreateBooking_Success() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(bookingRepository.countOverlappingBookings(any(), any(), any(), any(), any())).thenReturn(0L);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        Booking booking = bookingService.createBooking(bookingRequest, testUser);
        
        assertNotNull(booking);
        assertEquals("CONFIRMED", booking.getState());
        assertEquals(testItem, booking.getItem());
        assertEquals(testUser, booking.getUser());
    }
    
    @Test
    void testCreateBooking_ItemNotFound() {
        when(itemRepository.findById(1)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(bookingRequest, testUser);
        });
        
        assertEquals("Item não encontrado", exception.getMessage());
    }
    
    @Test
    void testGetUserBookings() {
        List<Booking> expectedBookings = Arrays.asList(new Booking(), new Booking());
        when(bookingRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(expectedBookings);
        
        List<Booking> result = bookingService.getUserBookings(testUser);
        
        assertEquals(2, result.size());
        verify(bookingRepository, times(1)).findByUserOrderByCreatedAtDesc(testUser);
    }
    
    @Test
    void testCheckAvailability() {
        when(bookingRepository.countOverlappingBookings(any(), any(), any(), any(), any())).thenReturn(0L);
        
        boolean available = bookingService.checkAvailability(1, bookingRequest.getStartUseDate(), bookingRequest.getEndUseDate());
        
        assertTrue(available);
    }
    
    @Test
    void testCalculatePrice() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        
        BigDecimal price = bookingService.calculatePrice(1, 3);
        
        assertEquals(new BigDecimal("75.00"), price); // 25.00 * 3
    }
}