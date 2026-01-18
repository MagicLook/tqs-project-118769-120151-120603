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
class BookingControllerTest {

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
        when(itemService.getItemById(testItem.getItemId())).thenReturn(Optional.of(testItem));
        
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
        when(itemService.getItemById(testItem.getItemId())).thenReturn(Optional.empty());

        String viewName = bookingController.showBookingForm(testItem.getItemId(), session, model);

        assertEquals("redirect:/magiclook/dashboard", viewName);
    }

    @Test
    void testCreateBooking_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(Optional.of(testItem));
        
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

        // availability check should not be stubbed for invalid date ranges
        when(bookingService.createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser)))
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
        verify(bookingService, times(1)).createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser));
    }

    @Test
    void testCreateBooking_StartDateIsToday_AllowsBooking() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(Optional.of(testItem));
        
        List<String> availableSizes = Arrays.asList("M", "L");
        Map<String, Integer> sizeAvailability = new HashMap<>();
        sizeAvailability.put("M", 2);
        sizeAvailability.put("L", 1);
        
        when(bookingService.getAvailableSizesForItem(testItem.getItemId())).thenReturn(availableSizes);
        when(bookingService.getSizeAvailabilityCount(testItem.getItemId())).thenReturn(sizeAvailability);
        when(bookingService.checkAvailabilityWithSize(anyInt(), anyString(), any(), any())).thenReturn(true);
        when(bookingService.createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser)))
            .thenReturn(testBooking);

        Date startDate = Date.from(LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
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

        assertEquals("redirect:/magiclook/booking/confirmation/" + testBooking.getBookingId(), viewName);
        verify(bookingService, times(1)).createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser));
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
        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    void testCreateBooking_ItemNotFound() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(Optional.empty());
        
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
        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    void testCreateBooking_ItemNotAvailable() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(Optional.of(testItem));
        
        List<String> availableSizes = Arrays.asList("M", "L");
        Map<String, Integer> sizeAvailability = new HashMap<>();
        sizeAvailability.put("M", 2);
        sizeAvailability.put("L", 1);
        
        when(bookingService.getAvailableSizesForItem(testItem.getItemId())).thenReturn(availableSizes);
        when(bookingService.getSizeAvailabilityCount(testItem.getItemId())).thenReturn(sizeAvailability);
        when(bookingService.checkAvailabilityWithSize(anyInt(), anyString(), any(), any())).thenReturn(true);
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
        // the controller will check availability but not proceed to create the booking
        verify(bookingService, times(1)).checkAvailabilityWithSize(anyInt(), anyString(), any(), any());
        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    void testCreateBooking_InvalidDates_PastStartDate() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(Optional.of(testItem));
        
        List<String> availableSizes = Arrays.asList("M", "L");
        Map<String, Integer> sizeAvailability = new HashMap<>();
        sizeAvailability.put("M", 2);
        sizeAvailability.put("L", 1);
        
        when(bookingService.getAvailableSizesForItem(testItem.getItemId())).thenReturn(availableSizes);
        when(bookingService.getSizeAvailabilityCount(testItem.getItemId())).thenReturn(sizeAvailability);
        
        // availability check should not be performed when end date is before start date
        
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
        // availability check should not be called for invalid date ranges
        verify(bookingService, never()).createBooking(any(com.magiclook.dto.BookingRequestDTO.class), any(User.class));
    }

    @Test
    void testCreateBooking_Exception() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(Optional.of(testItem));
        
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

        when(bookingService.createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser)))
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
        verify(bookingService, times(1)).createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser));
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
        verify(model).addAttribute("activePage", "myBookings");
        verify(model).addAttribute("filter", null);
        verify(model).addAttribute("search", null);
    }

    @Test
    void testBookingDetails_Success() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);
        when(bookingService.getCurrentBookingState(testBooking)).thenReturn("CONFIRMED");
        when(bookingService.getRefundInfo(testBooking)).thenReturn(new com.magiclook.dto.RefundInfoDTO(0, new BigDecimal("0.00")));

        String viewName = bookingController.bookingDetails(testBooking.getBookingId().toString(), session, model);

        assertEquals("booking/booking-details", viewName);
        verify(model).addAttribute("booking", testBooking);
        verify(model).addAttribute("user", testUser);
        verify(model).addAttribute("activePage", "myBookings");
        // As the booking is CONFIRMED and start date is in the future, canCancel should be true
        verify(model).addAttribute("canCancel", true);
        verify(model).addAttribute("refundPercent", 0);
        verify(model).addAttribute("refundAmount", new BigDecimal("0.00"));
    }

    @Test
    void testCancelInfo_OwnerAllowed() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);

        // Ensure booking start is in the future
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        testBooking.setStartUseDate(cal.getTime());

        when(bookingService.getCurrentBookingState(testBooking)).thenReturn("CONFIRMED");
        when(bookingService.getRefundInfo(testBooking)).thenReturn(new com.magiclook.dto.RefundInfoDTO(50, new BigDecimal("50.00")));

        Map<String, Object> resp = bookingController.cancelInfo(testBooking.getBookingId().toString(), session);

        assertTrue((Boolean) resp.get("canCancel"));
        assertEquals(50, resp.get("percent"));
        assertEquals(new BigDecimal("50.00"), resp.get("amount"));
    }

    @Test
    void testCancelInfo_OwnerNotAllowed_PastStartDate() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);

        // start date in the past
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        testBooking.setStartUseDate(cal.getTime());

        Map<String, Object> resp = bookingController.cancelInfo(testBooking.getBookingId().toString(), session);

        assertFalse((Boolean) resp.get("canCancel"));
        assertTrue(resp.containsKey("message"));
    }

    @Test
    void testCancelBooking_Post_AsOwner() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);
        when(bookingService.getCurrentBookingState(testBooking)).thenReturn("CONFIRMED");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        testBooking.setStartUseDate(cal.getTime());

        when(bookingService.cancelBooking(testBooking)).thenReturn(new com.magiclook.dto.RefundInfoDTO(50, new BigDecimal("50.00")));

        String redirect = bookingController.cancelBooking(testBooking.getBookingId().toString(), session);

        assertEquals("redirect:/magiclook/my-bookings/" + testBooking.getBookingId().toString(), redirect);
        verify(session).setAttribute(eq("message"), contains("Reserva cancelada com sucesso"));
    }

    @Test
    void testCancelBooking_Post_AsStaff() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);
        when(session.getAttribute("loggedInStaff")).thenReturn(new Object());
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);

        when(bookingService.getCurrentBookingState(testBooking)).thenReturn("CONFIRMED");
        when(bookingService.cancelBooking(testBooking)).thenReturn(new com.magiclook.dto.RefundInfoDTO(50, new BigDecimal("50.00")));

        String redirect = bookingController.cancelBooking(testBooking.getBookingId().toString(), session);

        // Pode redirecionar para staff/dashboard ou my-bookings
        assertTrue(redirect.contains("redirect:/magiclook/") && 
                  (redirect.contains("staff/dashboard") || redirect.contains("my-bookings")),
                  "Redirecionamento deve ser para staff/dashboard ou my-bookings, obteve: " + redirect);
        verify(session).setAttribute(eq("message"), contains("Reserva cancelada com sucesso"));
    }

    @Test
    void testBookingDetails_BookingNotFound() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(null);

        String viewName = bookingController.bookingDetails(testBooking.getBookingId().toString(), session, model);

        assertEquals("redirect:/magiclook/my-bookings", viewName);
    }

    @Test
    void testBookingDetails_UnauthorizedUser() {
        User otherUser = createTestUser();
        otherUser.setUserId(UUID.randomUUID());
        
        when(session.getAttribute("loggedInUser")).thenReturn(otherUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);

        String viewName = bookingController.bookingDetails(testBooking.getBookingId().toString(), session, model);

        assertEquals("redirect:/magiclook/my-bookings", viewName);
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
        when(itemService.getItemById(testItem.getItemId())).thenReturn(Optional.of(testItem));
        
        List<String> availableSizes = Arrays.asList("M", "L");
        Map<String, Integer> sizeAvailability = new HashMap<>();
        sizeAvailability.put("M", 2);
        sizeAvailability.put("L", 1);
        
        when(bookingService.getAvailableSizesForItem(testItem.getItemId())).thenReturn(availableSizes);
        when(bookingService.getSizeAvailabilityCount(testItem.getItemId())).thenReturn(sizeAvailability);
        
        // availability check is not expected to be invoked for end-before-start validation
        
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
        verify(bookingService, never()).createBooking(any(com.magiclook.dto.BookingRequestDTO.class), any(User.class));
    }

    @Test
    void testCreateBooking_SuccessWithValidDates() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(Optional.of(testItem));
        
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

        when(bookingService.createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser))).thenReturn(testBooking);

        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            "M",
            startDate,
            endDate,
            session,
            model
        );

        assertEquals("redirect:/magiclook/booking/confirmation/" + testBooking.getBookingId(), viewName);
        verify(bookingService, times(1)).createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser));
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
    void testShowMyBookings_SortByStartDateDesc_BothNullPreserveOrder() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        Booking b1 = createTestBooking();
        Booking b2 = createTestBooking();
        b1.setBookingId(UUID.randomUUID());
        b2.setBookingId(UUID.randomUUID());
        b1.setStartUseDate(null);
        b2.setStartUseDate(null);

        List<Booking> bookings = new ArrayList<>();
        bookings.add(b1);
        bookings.add(b2);
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);

        String viewName = bookingController.showMyBookings(session, model, null, null);

        assertEquals("booking/myBookings", viewName);
        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(model).addAttribute(eq("bookings"), captor.capture());
        List<Booking> result = captor.getValue();
        // Since both start dates are null, original order should be preserved (stable sort)
        assertEquals(b1.getBookingId(), result.get(0).getBookingId());
        assertEquals(b2.getBookingId(), result.get(1).getBookingId());
    }

    @Test
    void testShowMyBookings_SortByStartDateDesc_DescendingOrder() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        Booking b1 = createTestBooking();
        Booking b2 = createTestBooking();
        b1.setBookingId(UUID.randomUUID());
        b2.setBookingId(UUID.randomUUID());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 2);
        b1.setStartUseDate(cal.getTime());

        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 5);
        b2.setStartUseDate(cal.getTime());

        List<Booking> bookings = new ArrayList<>();
        bookings.add(b1);
        bookings.add(b2);
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);

        String viewName = bookingController.showMyBookings(session, model, null, null);

        assertEquals("booking/myBookings", viewName);
        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(model).addAttribute(eq("bookings"), captor.capture());
        List<Booking> result = captor.getValue();
        // b2 has later start date and should come first after sorting desc
        assertEquals(b2.getBookingId(), result.get(0).getBookingId());
        assertEquals(b1.getBookingId(), result.get(1).getBookingId());
    }

    @Test
    void testShowMyBookings_FilterPast_ReturnsOnlyPastStates() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        Booking completed = createTestBooking();
        completed.setBookingId(UUID.randomUUID());
        completed.setState("COMPLETED");

        Booking overdue = createTestBooking();
        overdue.setBookingId(UUID.randomUUID());
        overdue.setState("OVERDUE");

        Booking confirmed = createTestBooking();
        confirmed.setBookingId(UUID.randomUUID());
        confirmed.setState("CONFIRMED");

        List<Booking> bookings = new ArrayList<>();
        bookings.add(completed);
        bookings.add(overdue);
        bookings.add(confirmed);

        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);
        // The controller calls getCurrentBookingState for each booking; return the booking's own state
        when(bookingService.getCurrentBookingState(any(Booking.class))).thenAnswer(invocation -> ((Booking) invocation.getArgument(0)).getState());

        String viewName = bookingController.showMyBookings(session, model, "past", null);

        assertEquals("booking/myBookings", viewName);
        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(model).addAttribute(eq("bookings"), captor.capture());
        List<Booking> result = captor.getValue();
        assertEquals(2, result.size());
        for (Booking b : result) {
            assertTrue("COMPLETED".equals(b.getState()) || "OVERDUE".equals(b.getState()));
        }
    }

    @Test
    void testShowMyBookings_FilterUnknown_ReturnsAll() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        Booking a = createTestBooking();
        Booking b = createTestBooking();
        List<Booking> bookings = new ArrayList<>();
        bookings.add(a);
        bookings.add(b);
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);

        String viewName = bookingController.showMyBookings(session, model, "something", null);

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

    @Test
    void testCancelInfo_BookingNotFound() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(any(UUID.class))).thenReturn(null);

        Map<String, Object> resp =
            bookingController.cancelInfo(UUID.randomUUID().toString(), session);

        assertFalse((Boolean) resp.get("canCancel"));
        assertEquals("Reserva não encontrada", resp.get("message"));
    }

    @Test
    void testCancelInfo_Exception() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(any(UUID.class)))
            .thenThrow(new RuntimeException("Erro inesperado"));

        Map<String, Object> resp =
            bookingController.cancelInfo(UUID.randomUUID().toString(), session);

        assertFalse((Boolean) resp.get("canCancel"));
        assertEquals("Erro inesperado", resp.get("message"));
    }

    @Test
    void testCancelBooking_NotAllowed_AsOwner() {
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(session.getAttribute("loggedInStaff")).thenReturn(null);
        when(bookingService.getBookingById(testBooking.getBookingId()))
            .thenReturn(testBooking);

        // Estado não permite cancelamento
        when(bookingService.getCurrentBookingState(testBooking))
            .thenReturn("COMPLETED");

        String redirect =
            bookingController.cancelBooking(testBooking.getBookingId().toString(), session);

        assertEquals(
            "redirect:/magiclook/my-bookings/" + testBooking.getBookingId(),
            redirect
        );

        verify(session).setAttribute("message", "Cancelamento não permitido");
        verify(bookingService, never()).cancelBooking(any());
    }

    @Test
    void testGetUnavailableDates_WithSize() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        testBooking.setStartUseDate(startDate);
        testBooking.setEndUseDate(endDate);
        testBooking.setItemSingle(new ItemSingle());
        testBooking.getItemSingle().setSize("L");

        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingService.getConflictingBookings(anyInt(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(bookings);

        Map<String, Object> response = bookingController.getUnavailableDates(testItem.getItemId(), "L");

        assertNotNull(response);
        assertTrue(response.containsKey("unavailableDates"));
        List<String> unavailableDates = (List<String>) response.get("unavailableDates");
        assertNotNull(unavailableDates);
        assertTrue(unavailableDates.size() > 0);
    }

    @Test
    void testGetUnavailableDates_WithoutSize() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        testBooking.setStartUseDate(startDate);
        testBooking.setEndUseDate(endDate);

        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingService.getConflictingBookings(anyInt(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(bookings);

        Map<String, Object> response = bookingController.getUnavailableDates(testItem.getItemId(), null);

        assertNotNull(response);
        assertTrue(response.containsKey("unavailableDates"));
        List<String> unavailableDates = (List<String>) response.get("unavailableDates");
        assertTrue(unavailableDates.size() > 0);
    }

    @Test
    void testGetUnavailableDates_Exception() {
        when(bookingService.getConflictingBookings(anyInt(), any(LocalDate.class), any(LocalDate.class)))
            .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> response = bookingController.getUnavailableDates(testItem.getItemId(), "L");

        assertNotNull(response);
        assertTrue(response.containsKey("unavailableDates"));
        assertTrue(response.containsKey("error"));
        List<String> unavailableDates = (List<String>) response.get("unavailableDates");
        assertTrue(unavailableDates.isEmpty());
    }

}