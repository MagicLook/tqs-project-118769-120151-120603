package com.magiclook.booking;

import com.magiclook.boundary.BookingController;
import com.magiclook.data.*;
import com.magiclook.service.BookingService;
import com.magiclook.service.ItemService;
import com.magiclook.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private UserService userService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    private BookingController bookingController;

    private User testUser;
    private Item testItem;
    private Booking testBooking;
    private Shop testShop;
    private ItemType testItemType;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testShop = createTestShop();
        testItemType = createTestItemType();
        testItem = createTestItem();
        testBooking = createTestBooking();
        
        bookingController = new BookingController(bookingService, itemService, userService);
    }

    @Test
    void testShowBookingForm_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        
        List<String> availableSizes = Arrays.asList("M", "L");
        Map<String, Integer> sizeAvailability = new HashMap<>();
        sizeAvailability.put("M", 2);
        sizeAvailability.put("L", 1);
        
        when(bookingService.getAvailableSizesForItem(testItem.getItemId())).thenReturn(availableSizes);
        when(bookingService.getSizeAvailabilityCount(testItem.getItemId())).thenReturn(sizeAvailability);

        String viewName = bookingController.showBookingForm(testItem.getItemId(), session, model);

        assertEquals("booking/bookingForm", viewName);
        verify(model).addAttribute("item", testItem);
        verify(model).addAttribute("user", testUser);
        verify(model).addAttribute("availableSizes", availableSizes);
        verify(model).addAttribute("sizeAvailability", sizeAvailability);
    }

    @Test
    void testShowBookingForm_UserNotLoggedIn() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        String viewName = bookingController.showBookingForm(testItem.getItemId(), session, model);

        assertEquals("redirect:/magiclook/login", viewName);
        verify(session).setAttribute(eq("redirectAfterLogin"), anyString());
    }

    @Test
    void testShowBookingForm_ItemNotFound() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(null);

        String viewName = bookingController.showBookingForm(testItem.getItemId(), session, model);

        assertEquals("redirect:/magiclook/dashboard", viewName);
    }

    @Test
    void testCreateBooking_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        
        List<String> availableSizes = Arrays.asList("M", "L");
        Map<String, Integer> sizeAvailability = new HashMap<>();
        sizeAvailability.put("M", 2);
        sizeAvailability.put("L", 1);
        
        when(bookingService.getAvailableSizesForItem(testItem.getItemId())).thenReturn(availableSizes);
        when(bookingService.getSizeAvailabilityCount(testItem.getItemId())).thenReturn(sizeAvailability);
        
        when(bookingService.checkAvailabilityWithSize(anyInt(), anyString(), any(), any())).thenReturn(true);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.createBookingWithSize(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser)))
            .thenReturn(testBooking);

        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            "M",
            startDate,
            endDate,
            session,
            model
        );

        assertEquals("redirect:/magiclook/booking/confirmation/" + testBooking.getBookingId(), viewName);
        verify(bookingService, times(1)).checkAvailabilityWithSize(anyInt(), anyString(), any(), any());
        verify(bookingService, times(1)).createBookingWithSize(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser));
    }

    @Test
    void testCreateBooking_UserNotLoggedIn() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            "M",
            startDate,
            endDate,
            session,
            model
        );

        assertEquals("redirect:/magiclook/login", viewName);
        verify(bookingService, never()).checkAvailabilityWithSize(anyInt(), anyString(), any(), any());
        verify(bookingService, never()).createBookingWithSize(any(), any());
    }

    @Test
    void testCreateBooking_ItemNotFound() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(null);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            "M",
            startDate,
            endDate,
            session,
            model
        );

        assertEquals("redirect:/magiclook/dashboard", viewName);
        verify(model).addAttribute("error", "Item não encontrado.");
        verify(bookingService, never()).checkAvailabilityWithSize(anyInt(), anyString(), any(), any());
        verify(bookingService, never()).createBookingWithSize(any(), any());
    }

    @Test
    void testCreateBooking_ItemNotAvailable() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        
        List<String> availableSizes = Arrays.asList("M", "L");
        Map<String, Integer> sizeAvailability = new HashMap<>();
        sizeAvailability.put("M", 2);
        sizeAvailability.put("L", 1);
        
        when(bookingService.getAvailableSizesForItem(testItem.getItemId())).thenReturn(availableSizes);
        when(bookingService.getSizeAvailabilityCount(testItem.getItemId())).thenReturn(sizeAvailability);
        
        when(bookingService.checkAvailabilityWithSize(anyInt(), anyString(), any(), any())).thenReturn(false);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            "M",
            startDate,
            endDate,
            session,
            model
        );

        assertEquals("booking/bookingForm", viewName);
        verify(model).addAttribute("error", "Item não disponível nas datas selecionadas para o tamanho M");
        verify(model).addAttribute("item", testItem);
        verify(bookingService, times(1)).checkAvailabilityWithSize(anyInt(), anyString(), any(), any());
        verify(bookingService, never()).createBookingWithSize(any(), any());
    }

    @Test
    void testCreateBooking_InvalidDates_PastStartDate() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        
        List<String> availableSizes = Arrays.asList("M", "L");
        Map<String, Integer> sizeAvailability = new HashMap<>();
        sizeAvailability.put("M", 2);
        sizeAvailability.put("L", 1);
        
        when(bookingService.getAvailableSizesForItem(testItem.getItemId())).thenReturn(availableSizes);
        when(bookingService.getSizeAvailabilityCount(testItem.getItemId())).thenReturn(sizeAvailability);
        
        when(bookingService.checkAvailabilityWithSize(anyInt(), anyString(), any(), any())).thenReturn(true);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 5);
        Date endDate = cal.getTime();

        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            "M",
            startDate,
            endDate,
            session,
            model
        );

        assertEquals("booking/bookingForm", viewName);
        verify(model).addAttribute(eq("error"), anyString());
        verify(model).addAttribute("item", testItem);
        verify(bookingService, times(1)).checkAvailabilityWithSize(anyInt(), anyString(), any(), any());
        verify(bookingService, never()).createBookingWithSize(any(com.magiclook.dto.BookingRequestDTO.class), any(User.class));
    }

    @Test
    void testCreateBooking_Exception() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        
        List<String> availableSizes = Arrays.asList("M", "L");
        Map<String, Integer> sizeAvailability = new HashMap<>();
        sizeAvailability.put("M", 2);
        sizeAvailability.put("L", 1);
        
        when(bookingService.getAvailableSizesForItem(testItem.getItemId())).thenReturn(availableSizes);
        when(bookingService.getSizeAvailabilityCount(testItem.getItemId())).thenReturn(sizeAvailability);
        
        when(bookingService.checkAvailabilityWithSize(anyInt(), anyString(), any(), any())).thenReturn(true);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.createBookingWithSize(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser)))
            .thenThrow(new RuntimeException("Erro no banco de dados"));

        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            "M",
            startDate,
            endDate,
            session,
            model
        );

        assertEquals("booking/bookingForm", viewName);
        verify(model).addAttribute(eq("error"), anyString());
        verify(model).addAttribute("item", testItem);
        verify(bookingService, times(1)).checkAvailabilityWithSize(anyInt(), anyString(), any(), any());
    }

    @Test
    void testShowConfirmation_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);

        String viewName = bookingController.showConfirmation(testBooking.getBookingId(), session, model);

        assertEquals("booking/bookingConfirmation", viewName);
        verify(model).addAttribute("booking", testBooking);
        verify(model).addAttribute("user", testUser);
    }

    @Test
    void testShowConfirmation_UserNotLoggedIn() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        String viewName = bookingController.showConfirmation(testBooking.getBookingId(), session, model);

        assertEquals("redirect:/magiclook/login", viewName);
        verify(bookingService, never()).getBookingById(any());
    }

    @Test
    void testShowConfirmation_BookingNotFound() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(null);

        String viewName = bookingController.showConfirmation(testBooking.getBookingId(), session, model);

        assertEquals("redirect:/magiclook/dashboard", viewName);
    }

    @Test
    void testShowConfirmation_UnauthorizedUser() {
        User otherUser = createTestUser();
        otherUser.setUserId(UUID.randomUUID());
        
        when(session.getAttribute("loggedInUser")).thenReturn(otherUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);

        String viewName = bookingController.showConfirmation(testBooking.getBookingId(), session, model);

        assertEquals("redirect:/magiclook/dashboard", viewName);
    }

    @Test
    void testShowMyBookings_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(testBooking);
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);

        String viewName = bookingController.showMyBookings(session, model, null, null);

        assertEquals("booking/myBookings", viewName);
        verify(model).addAttribute("bookings", bookings);
        verify(model).addAttribute("user", testUser);
        verify(model).addAttribute("activePage", "/booking/myBookings");
        verify(model).addAttribute("filter", null);
        verify(model).addAttribute("search", null);
    }

    @Test
    void testBookingDetails_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);

        String viewName = bookingController.bookingDetails(testBooking.getBookingId().toString(), session, model);

        assertEquals("booking/booking-details", viewName);
        verify(model).addAttribute("booking", testBooking);
        verify(model).addAttribute("user", testUser);
        verify(model).addAttribute("activePage", "myBookings");
    }

    @Test
    void testBookingDetails_BookingNotFound() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(null);

        String viewName = bookingController.bookingDetails(testBooking.getBookingId().toString(), session, model);

        assertEquals("redirect:/magiclook/bookings/my-bookings", viewName);
    }

    @Test
    void testBookingDetails_UnauthorizedUser() {
        User otherUser = createTestUser();
        otherUser.setUserId(UUID.randomUUID());
        
        when(session.getAttribute("loggedInUser")).thenReturn(otherUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);

        String viewName = bookingController.bookingDetails(testBooking.getBookingId().toString(), session, model);

        assertEquals("redirect:/magiclook/bookings/my-bookings", viewName);
    }

    @Test
    void testBookingDetails_InvalidUUID() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);

        assertThrows(IllegalArgumentException.class, () -> {
            bookingController.bookingDetails("invalid-uuid", session, model);
        });
    }

    @Test
    void testCreateBooking_InvalidDates_EndBeforeStart() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        
        List<String> availableSizes = Arrays.asList("M", "L");
        Map<String, Integer> sizeAvailability = new HashMap<>();
        sizeAvailability.put("M", 2);
        sizeAvailability.put("L", 1);
        
        when(bookingService.getAvailableSizesForItem(testItem.getItemId())).thenReturn(availableSizes);
        when(bookingService.getSizeAvailabilityCount(testItem.getItemId())).thenReturn(sizeAvailability);
        
        when(bookingService.checkAvailabilityWithSize(anyInt(), anyString(), any(), any())).thenReturn(true);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, -10);
        Date endDate = cal.getTime();

        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            "M",
            startDate,
            endDate,
            session,
            model
        );

        assertEquals("booking/bookingForm", viewName);
        verify(model).addAttribute(eq("error"), anyString());
        verify(model).addAttribute("item", testItem);
        verify(bookingService, times(1)).checkAvailabilityWithSize(anyInt(), anyString(), any(), any());
        verify(bookingService, never()).createBookingWithSize(any(com.magiclook.dto.BookingRequestDTO.class), any(User.class));
    }

    @Test
    void testCreateBooking_SuccessWithValidDates() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        
        List<String> availableSizes = Arrays.asList("M", "L");
        Map<String, Integer> sizeAvailability = new HashMap<>();
        sizeAvailability.put("M", 2);
        sizeAvailability.put("L", 1);
        
        when(bookingService.getAvailableSizesForItem(testItem.getItemId())).thenReturn(availableSizes);
        when(bookingService.getSizeAvailabilityCount(testItem.getItemId())).thenReturn(sizeAvailability);
        
        when(bookingService.checkAvailabilityWithSize(anyInt(), anyString(), any(), any())).thenReturn(true);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.createBookingWithSize(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser))).thenReturn(testBooking);

        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            "M",
            startDate,
            endDate,
            session,
            model
        );

        assertEquals("redirect:/magiclook/booking/confirmation/" + testBooking.getBookingId(), viewName);
        verify(bookingService, times(1)).createBookingWithSize(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser));
        verify(bookingService, times(1)).checkAvailabilityWithSize(anyInt(), anyString(), any(), any());
    }

    @Test
    void testShowMyBookings_WithFilterActiveAndFutureDate() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        List<Booking> bookings = new ArrayList<>();
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        testBooking.setEndUseDate(cal.getTime());
        
        bookings.add(testBooking);
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);

        String viewName = bookingController.showMyBookings(session, model, "active", null);

        assertEquals("booking/myBookings", viewName);
        verify(model).addAttribute("filter", "active");
        verify(model).addAttribute(eq("bookings"), anyList());
    }

    @Test
    void testShowMyBookings_WithFilterActiveAndPastDate() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        List<Booking> bookings = new ArrayList<>();
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        testBooking.setEndUseDate(cal.getTime());
        
        bookings.add(testBooking);
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);

        String viewName = bookingController.showMyBookings(session, model, "active", null);

        assertEquals("booking/myBookings", viewName);
        verify(model).addAttribute("filter", "active");
        verify(model).addAttribute(eq("bookings"), anyList());
    }

    @Test
    void testShowMyBookings_WithSearchCaseInsensitive() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(testBooking);
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);

        String viewName = bookingController.showMyBookings(session, model, null, "CAMISA");

        assertEquals("booking/myBookings", viewName);
        verify(model).addAttribute("search", "CAMISA");
    }

    @Test
    void testShowMyBookings_OrderingWithNullDates() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        List<Booking> bookings = new ArrayList<>();
        
        Booking booking1 = createTestBooking();
        booking1.setStartUseDate(null);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Booking booking2 = createTestBooking();
        booking2.setBookingId(UUID.randomUUID());
        booking2.setStartUseDate(cal.getTime());
        
        bookings.add(booking1);
        bookings.add(booking2);
        
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);

        String viewName = bookingController.showMyBookings(session, model, null, null);

        assertEquals("booking/myBookings", viewName);
        verify(model).addAttribute(eq("bookings"), anyList());
    }

    @Test
    void testCheckAvailability_API_Success() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.checkAvailabilityWithSize(anyInt(), anyString(), any(), any())).thenReturn(true);
        when(bookingService.calculatePrice(anyInt(), anyLong())).thenReturn(new BigDecimal("75.00"));

        com.magiclook.dto.BookingRequestDTO requestDTO = new com.magiclook.dto.BookingRequestDTO();
        requestDTO.setItemId(testItem.getItemId());
        requestDTO.setSize("M");
        requestDTO.setStartUseDate(startDate);
        requestDTO.setEndUseDate(endDate);
        
        Map<String, Object> response = bookingController.checkAvailability(requestDTO);

        assertNotNull(response);
        assertTrue((Boolean) response.get("available"));
        assertEquals("Item disponível para o período selecionado", response.get("message"));
    }

    @Test
    void testCheckAvailability_API_NotAvailable() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.checkAvailabilityWithSize(anyInt(), anyString(), any(), any())).thenReturn(false);

        com.magiclook.dto.BookingRequestDTO requestDTO = new com.magiclook.dto.BookingRequestDTO();
        requestDTO.setItemId(testItem.getItemId());
        requestDTO.setSize("M");
        requestDTO.setStartUseDate(startDate);
        requestDTO.setEndUseDate(endDate);
        
        Map<String, Object> response = bookingController.checkAvailability(requestDTO);

        assertNotNull(response);
        assertFalse((Boolean) response.get("available"));
        assertEquals("Item não disponível para o período selecionado", response.get("message"));
    }

    @Test
    void testCheckAvailability_API_Exception() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.checkAvailabilityWithSize(anyInt(), anyString(), any(), any()))
            .thenThrow(new RuntimeException("Erro na verificação"));

        com.magiclook.dto.BookingRequestDTO requestDTO = new com.magiclook.dto.BookingRequestDTO();
        requestDTO.setItemId(testItem.getItemId());
        requestDTO.setSize("M");
        requestDTO.setStartUseDate(startDate);
        requestDTO.setEndUseDate(endDate);
        
        Map<String, Object> response = bookingController.checkAvailability(requestDTO);

        assertNotNull(response);
        assertFalse((Boolean) response.get("available"));
        assertNotNull(response.get("message"));
    }

    @Test
    void testCheckItemAvailability_API_Success() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.isItemAvailable(anyInt(), any(LocalDate.class), any(LocalDate.class))).thenReturn(true);

        Map<String, Object> response = bookingController.checkItemAvailability(
            testItem.getItemId(),
            null,
            startDate,
            endDate
        );

        assertNotNull(response);
        assertTrue((Boolean) response.get("available"));
    }

    @Test
    void testCheckItemAvailability_API_WithSize() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.isItemAvailableWithSize(anyInt(), anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(true);

        Map<String, Object> response = bookingController.checkItemAvailability(
            testItem.getItemId(),
            "M",
            startDate,
            endDate
        );

        assertNotNull(response);
        assertTrue((Boolean) response.get("available"));
    }

    @Test
    void testCheckItemAvailability_API_NotAvailable() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.isItemAvailable(anyInt(), any(LocalDate.class), any(LocalDate.class))).thenReturn(false);
        
        List<Booking> conflicts = new ArrayList<>();
        conflicts.add(testBooking);
        when(bookingService.getConflictingBookings(anyInt(), any(LocalDate.class), any(LocalDate.class))).thenReturn(conflicts);

        Map<String, Object> response = bookingController.checkItemAvailability(
            testItem.getItemId(),
            null,
            startDate,
            endDate
        );

        assertNotNull(response);
        assertFalse((Boolean) response.get("available"));
        assertTrue(response.containsKey("conflicts"));
    }

    @Test
    void testCheckItemAvailability_API_Exception() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.isItemAvailable(anyInt(), any(LocalDate.class), any(LocalDate.class)))
            .thenThrow(new RuntimeException("Erro ao verificar disponibilidade"));

        Map<String, Object> response = bookingController.checkItemAvailability(
            testItem.getItemId(),
            null,
            startDate,
            endDate
        );

        assertNotNull(response);
        assertFalse((Boolean) response.get("available"));
        assertTrue(response.containsKey("error"));
    }

    private User createTestUser() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        return user;
    }

    private Shop createTestShop() {
        Shop shop = new Shop();
        shop.setShopId(1);
        shop.setName("Loja Central");
        shop.setLocation("Lisboa");
        return shop;
    }

    private ItemType createTestItemType() {
        ItemType itemType = new ItemType();
        itemType.setId(1);
        itemType.setGender("M");
        itemType.setCategory("Shirt");
        itemType.setSubcategory("Casual");
        return itemType;
    }

    private Item createTestItem() {
        Item item = new Item();
        item.setItemId(1);
        item.setName("Camisa Azul Elegante");
        item.setBrand("Zara");
        item.setMaterial("Algodão");
        item.setColor("Azul");
        item.setPriceRent(new BigDecimal("25.00"));
        item.setPriceSale(new BigDecimal("75.00"));
        item.setImagePath("/images/camisa-azul.jpg");
        item.setAvailable(true);
        item.setShop(testShop);
        item.setItemType(testItemType);
        return item;
    }

    private Booking createTestBooking() {
        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date pickupDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date startUseDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 3);
        Date endUseDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date returnDate = cal.getTime();
        
        booking.setPickupDate(pickupDate);
        booking.setStartUseDate(startUseDate);
        booking.setEndUseDate(endUseDate);
        booking.setReturnDate(returnDate);
        booking.setTotalDays(3);
        booking.setTotalPrice(new BigDecimal("75.00"));
        booking.setState("CONFIRMED");
        booking.setItem(testItem);
        booking.setUser(testUser);
        booking.setCreatedAt(new Date());
        
        return booking;
    }
}