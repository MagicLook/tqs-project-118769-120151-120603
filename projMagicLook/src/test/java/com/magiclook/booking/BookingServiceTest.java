package com.magiclook.booking;

import com.magiclook.data.*;
import com.magiclook.dto.BookingRequestDTO;
import com.magiclook.repository.BookingRepository;
import com.magiclook.repository.ItemRepository;
import com.magiclook.repository.ItemSingleRepository;
import com.magiclook.repository.UserRepository;
import com.magiclook.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // Make mocks lenient to avoid strict stubbing issues
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemSingleRepository itemSingleRepository;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private Item testItem;
    private Booking testBooking;
    private BookingRequestDTO bookingRequest;
    private ItemSingle testItemSingle;
    private UUID itemSingleId;

    @BeforeEach
    void setUp() throws Exception {
        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        
        testItem = new Item();
        testItem.setItemId(1);
        testItem.setPriceRent(new BigDecimal("25.00"));
        
        // Create ItemSingle and set its ID using reflection
        testItemSingle = new ItemSingle("AVAILABLE", testItem, "M");
        itemSingleId = UUID.randomUUID();
        Field idField = ItemSingle.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testItemSingle, itemSingleId);
        
        testBooking = new Booking();
        testBooking.setBookingId(UUID.randomUUID());
        testBooking.setUser(testUser);
        testBooking.setItem(testItem);
        testBooking.setItemSingle(testItemSingle);
        testBooking.setState("CONFIRMED");
        testBooking.setTotalPrice(new BigDecimal("75.00"));
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        testBooking.setStartUseDate(cal.getTime());
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        testBooking.setEndUseDate(cal.getTime());
        testBooking.setCreatedAt(new Date());
        
        bookingRequest = new BookingRequestDTO();
        bookingRequest.setItemId(1);
        bookingRequest.setSize("M");
        bookingRequest.setStartUseDate(testBooking.getStartUseDate());
        bookingRequest.setEndUseDate(testBooking.getEndUseDate());
        
        // Set up common lenient stubs to avoid strict stubbing issues
        lenient().when(bookingRepository.countOverlappingBookingsForItemSingle(
            any(UUID.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class)))
            .thenReturn(0L);
    }

    @Test
    void testCreateBooking_Success() {
        when(itemRepository.findById(bookingRequest.getItemId()))
            .thenReturn(Optional.of(testItem));
            
        // Mock para ItemSingleRepository - retornar lista com um ItemSingle disponível
        List<ItemSingle> itemSingles = new ArrayList<>();
        itemSingles.add(testItemSingle);
        when(itemSingleRepository.findByItem_ItemId(bookingRequest.getItemId()))
            .thenReturn(itemSingles);
        
        // Mock the save method
        when(bookingRepository.save(any(Booking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.createBooking(bookingRequest, testUser);

        assertNotNull(result);
        assertEquals(testItem, result.getItem());
        assertEquals(testUser, result.getUser());
        assertEquals("CONFIRMED", result.getState());
        assertNotNull(result.getCreatedAt());
        
        verify(itemRepository, times(1)).findById(bookingRequest.getItemId());
        verify(itemSingleRepository, times(1)).findByItem_ItemId(bookingRequest.getItemId());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_ItemNotFound() {
        when(itemRepository.findById(bookingRequest.getItemId()))
            .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(bookingRequest, testUser);
        });

        assertEquals("Item não encontrado", exception.getMessage());
        verify(itemRepository, times(1)).findById(bookingRequest.getItemId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_UserNotAuthenticated() {
        when(itemRepository.findById(bookingRequest.getItemId()))
            .thenReturn(Optional.of(testItem));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(bookingRequest, null);
        });

        assertEquals("Utilizador não autenticado", exception.getMessage());
        verify(itemRepository, times(1)).findById(bookingRequest.getItemId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_InvalidDates() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, -10);
        Date endDate = cal.getTime();
        
        bookingRequest.setStartUseDate(startDate);
        bookingRequest.setEndUseDate(endDate);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(bookingRequest, testUser);
        });

        assertEquals("Datas inválidas", exception.getMessage());
        verify(itemRepository, never()).findById(anyInt());
    }

    @Test
    void testCreateBooking_ItemNotAvailable() {
        when(itemRepository.findById(bookingRequest.getItemId()))
            .thenReturn(Optional.of(testItem));
            
        // CORREÇÃO: Lista vazia significa nenhum ItemSingle disponível
        when(itemSingleRepository.findByItem_ItemId(bookingRequest.getItemId()))
            .thenReturn(new ArrayList<>());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(bookingRequest, testUser);
        });

        // A mensagem de erro deve conter alguma indicação de não disponibilidade
        String message = exception.getMessage().toLowerCase();
        // Verificar se contém alguma das palavras-chave (pode variar)
        boolean hasErrorKeyword = message.contains("não disponível") || 
                   message.contains("indisponível") ||
                   message.contains("disponível") ||
                   message.contains("available") ||
                   message.contains("unavailable");
        
        // Se a mensagem não contém nenhuma palavra-chave, falha o teste com mensagem clara
        if (!hasErrorKeyword) {
            fail("A mensagem de erro deveria conter indicação de não disponibilidade. Mensagem: " + message);
        }
        
        verify(itemRepository, times(1)).findById(bookingRequest.getItemId());
        verify(itemSingleRepository, times(1)).findByItem_ItemId(bookingRequest.getItemId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testGetUserBookings_Success() {
        List<Booking> expectedBookings = new ArrayList<>();
        expectedBookings.add(testBooking);
        
        when(bookingRepository.findByUserOrderByCreatedAtDesc(testUser))
            .thenReturn(expectedBookings);

        List<Booking> result = bookingService.getUserBookings(testUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBooking, result.get(0));
        verify(bookingRepository, times(1)).findByUserOrderByCreatedAtDesc(testUser);
    }

    @Test
    void testGetUserBookings_UserNull() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.getUserBookings(null);
        });

        assertEquals("Utilizador não autenticado", exception.getMessage());
        verify(bookingRepository, never()).findByUserOrderByCreatedAtDesc(any());
    }

    @Test
    void testGetUserBookings_EmptyList() {
        when(bookingRepository.findByUserOrderByCreatedAtDesc(testUser))
            .thenReturn(new ArrayList<>());

        List<Booking> result = bookingService.getUserBookings(testUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findByUserOrderByCreatedAtDesc(testUser);
    }

    @Test
    void testCheckAvailability_Available() {
        // Mock para ItemSingleRepository - retornar ItemSingle disponível
        List<ItemSingle> itemSingles = new ArrayList<>();
        itemSingles.add(testItemSingle);
        
        when(itemSingleRepository.findByItem_ItemId(testItem.getItemId()))
            .thenReturn(itemSingles);
            
        // Override the common stub for this test
        when(bookingRepository.countOverlappingBookingsForItemSingle(
            eq(itemSingleId), any(Date.class), any(Date.class), any(Date.class), any(Date.class)))
            .thenReturn(0L);

        boolean result = bookingService.checkAvailability(
            testItem.getItemId(), 
            bookingRequest.getStartUseDate(), 
            bookingRequest.getEndUseDate()
        );

        assertTrue(result);
        verify(itemSingleRepository, times(1)).findByItem_ItemId(testItem.getItemId());
    }

    @Test
    void testCheckAvailability_NotAvailable() {
        // Mock para ItemSingleRepository - retornar ItemSingle disponível
        List<ItemSingle> itemSingles = new ArrayList<>();
        itemSingles.add(testItemSingle);
        
        when(itemSingleRepository.findByItem_ItemId(testItem.getItemId()))
            .thenReturn(itemSingles);

        // Override the common stub for this test
        when(bookingRepository.countOverlappingBookingsForItemSingle(
            eq(itemSingleId), any(Date.class), any(Date.class), any(Date.class), any(Date.class)))
            .thenReturn(1L);

        boolean result = bookingService.checkAvailability(
            testItem.getItemId(), 
            bookingRequest.getStartUseDate(), 
            bookingRequest.getEndUseDate()
        );

        assertFalse(result);
        verify(itemSingleRepository, times(1)).findByItem_ItemId(testItem.getItemId());
    }

    @Test
    void testGetBookingById_Success() {
        when(bookingRepository.findById(testBooking.getBookingId()))
            .thenReturn(Optional.of(testBooking));

        Booking result = bookingService.getBookingById(testBooking.getBookingId());

        assertNotNull(result);
        assertEquals(testBooking.getBookingId(), result.getBookingId());
        assertEquals(testUser, result.getUser());
        assertEquals(testItem, result.getItem());
        verify(bookingRepository, times(1)).findById(testBooking.getBookingId());
    }

    @Test
    void testGetBookingById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(bookingRepository.findById(nonExistentId))
            .thenReturn(Optional.empty());

        Booking result = bookingService.getBookingById(nonExistentId);

        assertNull(result);
        verify(bookingRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testIsItemAvailable_Success() {
        LocalDate startDate = LocalDate.now().plusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(10);
        
        // Mock para ItemSingleRepository - retornar ItemSingle disponível
        List<ItemSingle> itemSingles = new ArrayList<>();
        itemSingles.add(testItemSingle);
        
        when(itemSingleRepository.findByItem_ItemId(testItem.getItemId()))
            .thenReturn(itemSingles);
            
        // Override the common stub for this test
        when(bookingRepository.countOverlappingBookingsForItemSingle(
            eq(itemSingleId), any(Date.class), any(Date.class), any(Date.class), any(Date.class)))
            .thenReturn(0L);

        boolean result = bookingService.isItemAvailable(testItem.getItemId(), startDate, endDate);

        assertTrue(result);
        verify(itemSingleRepository, times(1)).findByItem_ItemId(testItem.getItemId());
    }

    @Test
    void testIsItemAvailable_NotAvailable() {
        LocalDate startDate = LocalDate.now().plusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(10);
        
        // Mock para ItemSingleRepository - lista vazia
        when(itemSingleRepository.findByItem_ItemId(testItem.getItemId()))
            .thenReturn(new ArrayList<>());

        boolean result = bookingService.isItemAvailable(testItem.getItemId(), startDate, endDate);

        assertFalse(result);
        verify(itemSingleRepository, times(1)).findByItem_ItemId(testItem.getItemId());
    }

    @Test
    void testGetConflictingBookings_Success() {
        LocalDate startDate = LocalDate.now().plusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(10);
        
        List<Booking> conflictingBookings = new ArrayList<>();
        conflictingBookings.add(testBooking);
        
        when(bookingRepository.findOverlappingBookings(
            anyInt(), any(Date.class), any(Date.class), any(Date.class), any(Date.class)))
            .thenReturn(conflictingBookings);

        List<Booking> result = bookingService.getConflictingBookings(
            testItem.getItemId(), startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBooking, result.get(0));
    }

    @Test
    void testGetConflictingBookings_NoConflicts() {
        LocalDate startDate = LocalDate.now().plusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(10);
        
        when(bookingRepository.findOverlappingBookings(
            anyInt(), any(Date.class), any(Date.class), any(Date.class), any(Date.class)))
            .thenReturn(new ArrayList<>());

        List<Booking> result = bookingService.getConflictingBookings(
            testItem.getItemId(), startDate, endDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveBooking() {
        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID());
        booking.setUser(testUser);
        booking.setItem(testItem);
        when(bookingRepository.save(booking)).thenReturn(booking);

        bookingService.saveBooking(booking);

        verify(bookingRepository, times(1)).save(booking);
    }
    
    @Test
    void testCheckItemAvailability() {
        Integer itemId = 1;
        LocalDate start = LocalDate.now().plusDays(7);
        LocalDate end = LocalDate.now().plusDays(10);
        
        // Mock para ItemSingleRepository
        List<ItemSingle> itemSingles = new ArrayList<>();
        itemSingles.add(testItemSingle);
        
        when(itemSingleRepository.findByItem_ItemId(itemId))
            .thenReturn(itemSingles);
        
        // Override the common stub for this test
        when(bookingRepository.countOverlappingBookingsForItemSingle(
            eq(itemSingleId), any(Date.class), any(Date.class), any(Date.class), any(Date.class)))
            .thenReturn(0L);
        
        // Chamar o método
        boolean result = bookingService.checkItemAvailability(itemId, start, end);
        
        // Verificar que o método necessário foi chamado
        verify(itemSingleRepository, times(1)).findByItem_ItemId(itemId);
        assertTrue(result);
    }

    @Test
    void testGetCurrentBookingState_Cancelled() {
        Booking booking = new Booking();
        booking.setState("CANCELLED");
        booking.setStartUseDate(new Date());
        booking.setEndUseDate(new Date());
        
        String state = bookingService.getCurrentBookingState(booking);
        
        assertEquals("CANCELLED", state);
    }

    @Test
    void testGetCurrentBookingState_Completed() {
        Booking booking = new Booking();
        booking.setState("COMPLETED");
        booking.setStartUseDate(new Date());
        booking.setEndUseDate(new Date());
        
        String state = bookingService.getCurrentBookingState(booking);
        
        assertEquals("COMPLETED", state);
    }

    @Test
    void testGetCurrentBookingState_Confirmed() {
        Booking booking = new Booking();
        booking.setState(null);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        booking.setStartUseDate(cal.getTime());
        
        cal.add(Calendar.DAY_OF_MONTH, 3);
        booking.setEndUseDate(cal.getTime());
        
        String state = bookingService.getCurrentBookingState(booking);
        
        assertEquals("CONFIRMED", state);
    }

    @Test
    void testGetCurrentBookingState_Active() {
        Booking booking = new Booking();
        booking.setState(null);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        booking.setStartUseDate(cal.getTime());
        
        cal.add(Calendar.DAY_OF_MONTH, 5);
        booking.setEndUseDate(cal.getTime());
        
        String state = bookingService.getCurrentBookingState(booking);
        
        assertEquals("ACTIVE", state);
    }

    @Test
    void testGetCurrentBookingState_Overdue() {
        Booking booking = new Booking();
        booking.setState(null);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -10);
        booking.setStartUseDate(cal.getTime());
        
        cal.add(Calendar.DAY_OF_MONTH, 3);
        booking.setEndUseDate(cal.getTime());
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date returnDate = cal.getTime();
        booking.setReturnDate(returnDate);
        
        String state = bookingService.getCurrentBookingState(booking);
        
        assertEquals("OVERDUE", state);
    }

    @Test
    void testGetCurrentBookingState_CompletedAfterEndDate() {
        Booking booking = new Booking();
        booking.setState(null);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -10);
        booking.setStartUseDate(cal.getTime());
        
        cal.add(Calendar.DAY_OF_MONTH, 3);
        booking.setEndUseDate(cal.getTime());
        
        cal.add(Calendar.DAY_OF_MONTH, 10);
        Date returnDate = cal.getTime();
        booking.setReturnDate(returnDate);
        
        String state = bookingService.getCurrentBookingState(booking);
        
        assertEquals("COMPLETED", state);
    }

    @Test
    void testGetCurrentBookingState_ReturnedState() {
        Booking booking = new Booking();
        booking.setState("RETURNED");
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -20);
        booking.setStartUseDate(cal.getTime());
        
        cal.add(Calendar.DAY_OF_MONTH, 10);
        booking.setEndUseDate(cal.getTime());
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date returnDate = cal.getTime();
        booking.setReturnDate(returnDate);
        
        String state = bookingService.getCurrentBookingState(booking);
        
        assertEquals("COMPLETED", state);
    }

    @Test
    void testGetCurrentBookingState_NullBooking() {
        assertThrows(NullPointerException.class, () -> {
            bookingService.getCurrentBookingState(null);
        });
    }

    @Test
    void testGetCurrentBookingState_NullDates() {
        Booking booking = new Booking();
        booking.setState(null);
        
        assertThrows(NullPointerException.class, () -> {
            bookingService.getCurrentBookingState(booking);
        });
    }

    @Test
    void testGetRefundInfo_100Percent() {
        testBooking.setTotalPrice(new BigDecimal("200.00"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 40); // far in the future
        testBooking.setStartUseDate(cal.getTime());

        com.magiclook.dto.RefundInfoDTO info = bookingService.getRefundInfo(testBooking);
        assertEquals(100, info.getPercent());
        assertEquals(new BigDecimal("200.00"), info.getAmount());
    }

    @Test
    void testGetRefundInfo_50Percent() {
        testBooking.setTotalPrice(new BigDecimal("200.00"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 20);
        testBooking.setStartUseDate(cal.getTime());

        com.magiclook.dto.RefundInfoDTO info = bookingService.getRefundInfo(testBooking);
        assertEquals(50, info.getPercent());
        assertEquals(new BigDecimal("100.00"), info.getAmount());
    }

    @Test
    void testGetRefundInfo_25Percent() {
        testBooking.setTotalPrice(new BigDecimal("80.00"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 10);
        testBooking.setStartUseDate(cal.getTime());

        com.magiclook.dto.RefundInfoDTO info = bookingService.getRefundInfo(testBooking);
        assertEquals(25, info.getPercent());
        assertEquals(new BigDecimal("20.00"), info.getAmount());
    }

    @Test
    void testGetRefundInfo_0Percent_Within48h() {
        testBooking.setTotalPrice(new BigDecimal("80.00"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 24); // less than 48 hours
        testBooking.setStartUseDate(cal.getTime());

        com.magiclook.dto.RefundInfoDTO info = bookingService.getRefundInfo(testBooking);
        assertEquals(0, info.getPercent());
        assertEquals(new BigDecimal("0.00"), info.getAmount());
    }

    @Test
    void testCancelBooking_ChangesStateAndReturnsRefund() {
        testBooking.setTotalPrice(new BigDecimal("100.00"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 20);
        testBooking.setStartUseDate(cal.getTime());
        testBooking.setState("CONFIRMED");

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.magiclook.dto.RefundInfoDTO info = bookingService.cancelBooking(testBooking);

        assertEquals("CANCELLED", testBooking.getState());
        assertEquals(50, info.getPercent());
        assertEquals(new BigDecimal("50.00"), info.getAmount());
        verify(bookingRepository, times(1)).save(testBooking);
    }
}