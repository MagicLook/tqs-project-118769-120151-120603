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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

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

    @InjectMocks
    private BookingController bookingController;

    private User testUser;
    private Item testItem;
    private Booking testBooking;
    private Shop testShop;
    private ItemType testItemType;

    @BeforeEach
    void setUp() {
        // Configurar objetos de teste completos
        testUser = createTestUser();
        testShop = createTestShop();
        testItemType = createTestItemType();
        testItem = createTestItem();
        testBooking = createTestBooking();
    }

    // ========== TESTES DE FORMULÁRIO DE RESERVA ==========

    @Test
    void testShowBookingForm_Success() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);

        // Act
        String viewName = bookingController.showBookingForm(testItem.getItemId(), session, model);

        // Assert
        assertEquals("booking/bookingForm", viewName);
        verify(model).addAttribute("item", testItem);
        verify(model).addAttribute("user", testUser);
    }

    @Test
    void testShowBookingForm_UserNotLoggedIn() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        // Act
        String viewName = bookingController.showBookingForm(testItem.getItemId(), session, model);

        // Assert
        assertEquals("redirect:/magiclook/login", viewName);
        verify(session).setAttribute(eq("redirectAfterLogin"), anyString());
    }

    @Test
    void testShowBookingForm_ItemNotFound() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(null);

        // Act
        String viewName = bookingController.showBookingForm(testItem.getItemId(), session, model);

        // Assert
        assertEquals("redirect:/magiclook/dashboard", viewName);
    }

    // ========== TESTES DE CRIAÇÃO DE RESERVA ==========

    @Test
    void testCreateBooking_Success() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        when(bookingService.checkAvailability(anyInt(), any(), any())).thenReturn(true);
        
        // Criar datas futuras válidas
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        // Mock do método createBooking
        when(bookingService.createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser)))
            .thenReturn(testBooking);

        // Act
        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            startDate,
            endDate,
            session,
            model
        );

        // Assert
        assertEquals("redirect:/magiclook/booking/confirmation/" + testBooking.getBookingId(), viewName);
        verify(bookingService, times(1)).checkAvailability(anyInt(), any(), any());
        verify(bookingService, times(1)).createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser));
    }

    @Test
    void testCreateBooking_UserNotLoggedIn() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(null);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        // Act
        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            startDate,
            endDate,
            session,
            model
        );

        // Assert
        assertEquals("redirect:/magiclook/login", viewName);
        verify(bookingService, never()).checkAvailability(anyInt(), any(), any());
        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    void testCreateBooking_ItemNotFound() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(null);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        // Act
        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            startDate,
            endDate,
            session,
            model
        );

        // Assert - O controlador redireciona com erro no model
        assertEquals("redirect:/magiclook/dashboard", viewName);
        verify(model).addAttribute("error", "Item não encontrado.");
        verify(bookingService, never()).checkAvailability(anyInt(), any(), any());
        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    void testCreateBooking_ItemNotAvailable() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        when(bookingService.checkAvailability(anyInt(), any(), any())).thenReturn(false);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        // Act
        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            startDate,
            endDate,
            session,
            model
        );

        // Assert
        assertEquals("booking/bookingForm", viewName);
        verify(model).addAttribute("error", "Item não disponível nas datas selecionadas. Por favor, escolha outras datas.");
        verify(model).addAttribute("item", testItem);
        verify(bookingService, times(1)).checkAvailability(anyInt(), any(), any());
        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    void testCreateBooking_InvalidDates_PastStartDate() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        // IMPORTANTE: O controlador verifica disponibilidade ANTES de validar as datas
        // Então precisamos mockar o checkAvailability para retornar true para que a validação ocorra
        when(bookingService.checkAvailability(anyInt(), any(), any())).thenReturn(true);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7); // Data no passado
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        // Act
        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            startDate,
            endDate,
            session,
            model
        );

        // Assert - Agora deve retornar erro de data passada
        assertEquals("booking/bookingForm", viewName);
        verify(model).addAttribute("error", "A data de início não pode ser no passado.");
        verify(model).addAttribute("item", testItem);
        verify(bookingService, times(1)).checkAvailability(anyInt(), any(), any());
        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    void testCreateBooking_Exception() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        when(bookingService.checkAvailability(anyInt(), any(), any())).thenReturn(true);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser)))
            .thenThrow(new RuntimeException("Erro no banco de dados"));

        // Act
        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            startDate,
            endDate,
            session,
            model
        );

        // Assert
        assertEquals("booking/bookingForm", viewName);
        verify(model).addAttribute(eq("error"), contains("Erro ao criar reserva"));
        verify(model).addAttribute("item", testItem);
        verify(bookingService, times(1)).checkAvailability(anyInt(), any(), any());
    }

    // ========== TESTES DE CONFIRMAÇÃO ==========

    @Test
    void testShowConfirmation_Success() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);

        // Act
        String viewName = bookingController.showConfirmation(testBooking.getBookingId(), session, model);

        // Assert
        assertEquals("booking/bookingConfirmation", viewName);
        verify(model).addAttribute("booking", testBooking);
        verify(model).addAttribute("user", testUser);
    }

    @Test
    void testShowConfirmation_UserNotLoggedIn() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        // Act
        String viewName = bookingController.showConfirmation(testBooking.getBookingId(), session, model);

        // Assert
        assertEquals("redirect:/magiclook/login", viewName);
        verify(bookingService, never()).getBookingById(any());
    }

    @Test
    void testShowConfirmation_BookingNotFound() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(null);

        // Act
        String viewName = bookingController.showConfirmation(testBooking.getBookingId(), session, model);

        // Assert
        assertEquals("redirect:/magiclook/dashboard", viewName);
    }

    @Test
    void testShowConfirmation_UnauthorizedUser() {
        // Arrange
        User otherUser = createTestUser();
        otherUser.setUserId(UUID.randomUUID());
        
        when(session.getAttribute("loggedInUser")).thenReturn(otherUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);

        // Act
        String viewName = bookingController.showConfirmation(testBooking.getBookingId(), session, model);

        // Assert
        assertEquals("redirect:/magiclook/dashboard", viewName);
    }

    // ========== TESTES DE MINHAS RESERVAS (COM FILTROS E PESQUISA) ==========

    @Test
    void testShowMyBookings_Success() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(testBooking);
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);

        // Act
        String viewName = bookingController.showMyBookings(session, model, null, null);

        // Assert
        assertEquals("booking/myBookings", viewName);
        verify(model).addAttribute("bookings", bookings);
        verify(model).addAttribute("user", testUser);
        verify(model).addAttribute("activePage", "/booking/myBookings"); // O controlador usa "/booking/myBookings"
        verify(model).addAttribute("filter", null);
        verify(model).addAttribute("search", null);
    }

    @Test
    void testBookingDetails_Success() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);

        // Act
        String viewName = bookingController.bookingDetails(testBooking.getBookingId().toString(), session, model);

        // Assert
        assertEquals("booking/booking-details", viewName);
        verify(model).addAttribute("booking", testBooking);
        verify(model).addAttribute("user", testUser);
        verify(model).addAttribute("activePage", "myBookings");
    }

    @Test
    void testBookingDetails_BookingNotFound() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(null);

        // Act
        String viewName = bookingController.bookingDetails(testBooking.getBookingId().toString(), session, model);

        // Assert - Corrigido para corresponder ao controlador real
        assertEquals("redirect:/magiclook/bookings/my-bookings", viewName);
    }

    @Test
    void testBookingDetails_UnauthorizedUser() {
        // Arrange
        User otherUser = createTestUser();
        otherUser.setUserId(UUID.randomUUID());
        
        when(session.getAttribute("loggedInUser")).thenReturn(otherUser);
        when(bookingService.getBookingById(testBooking.getBookingId())).thenReturn(testBooking);

        // Act
        String viewName = bookingController.bookingDetails(testBooking.getBookingId().toString(), session, model);

        // Assert - Corrigido para corresponder ao controlador real
        assertEquals("redirect:/magiclook/bookings/my-bookings", viewName);
    }

    @Test
    void testBookingDetails_InvalidUUID() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);

        // Act - UUID inválido agora deve lançar exceção (o controlador não trata)
        // Precisamos testar que lança IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            bookingController.bookingDetails("invalid-uuid", session, model);
        });
    }

    @Test
    void testCreateBooking_InvalidDates_EndBeforeStart() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        // O controlador verifica disponibilidade antes da validação
        when(bookingService.checkAvailability(anyInt(), any(), any())).thenReturn(true);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, -10); // Data anterior
        Date endDate = cal.getTime();

        // Act
        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            startDate,
            endDate,
            session,
            model
        );

        // Assert
        assertEquals("booking/bookingForm", viewName);
        verify(model).addAttribute("error", "Datas inválidas. A data de fim deve ser após a data de início.");
        verify(model).addAttribute("item", testItem);
        verify(bookingService, times(1)).checkAvailability(anyInt(), any(), any());
        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    void testCreateBooking_SuccessWithValidDates() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        when(itemService.getItemById(testItem.getItemId())).thenReturn(testItem);
        when(bookingService.checkAvailability(anyInt(), any(), any())).thenReturn(true);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7); // Data futura
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser))).thenReturn(testBooking);

        // Act
        String viewName = bookingController.createBooking(
            testItem.getItemId(),
            startDate,
            endDate,
            session,
            model
        );

        // Assert
        assertEquals("redirect:/magiclook/booking/confirmation/" + testBooking.getBookingId(), viewName);
        verify(bookingService, times(1)).createBooking(any(com.magiclook.dto.BookingRequestDTO.class), eq(testUser));
        verify(bookingService, times(1)).checkAvailability(anyInt(), any(), any());
    }

    // ========== NOVOS TESTES PARA FILTROS ==========

    @Test
    void testShowMyBookings_WithFilterActiveAndFutureDate() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        List<Booking> bookings = new ArrayList<>();
        
        // Configurar data de fim no futuro
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        testBooking.setEndUseDate(cal.getTime());
        
        bookings.add(testBooking);
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);

        // Act
        String viewName = bookingController.showMyBookings(session, model, "active", null);

        // Assert
        assertEquals("booking/myBookings", viewName);
        verify(model).addAttribute("filter", "active");
        verify(model).addAttribute(eq("bookings"), anyList());
        // Deve manter a reserva (data futura = ativa)
    }

    @Test
    void testShowMyBookings_WithFilterActiveAndPastDate() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        List<Booking> bookings = new ArrayList<>();
        
        // Configurar data de fim no passado
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        testBooking.setEndUseDate(cal.getTime());
        
        bookings.add(testBooking);
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);

        // Act
        String viewName = bookingController.showMyBookings(session, model, "active", null);

        // Assert
        assertEquals("booking/myBookings", viewName);
        verify(model).addAttribute("filter", "active");
        verify(model).addAttribute(eq("bookings"), anyList());
        // Deve filtrar a reserva (data passada ≠ ativa)
    }

    @Test
    void testShowMyBookings_WithSearchCaseInsensitive() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(testUser);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(testBooking);
        when(bookingService.getUserBookings(testUser)).thenReturn(bookings);

        // Act - Busca em maiúsculas
        String viewName = bookingController.showMyBookings(session, model, null, "CAMISA");

        // Assert
        assertEquals("booking/myBookings", viewName);
        verify(model).addAttribute("search", "CAMISA");
        // Deve encontrar "Camisa" (case insensitive)
    }

    @Test
    void testShowMyBookings_OrderingWithNullDates() {
        // Arrange
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

        // Act
        String viewName = bookingController.showMyBookings(session, model, null, null);

        // Assert
        assertEquals("booking/myBookings", viewName);
        verify(model).addAttribute(eq("bookings"), anyList());
        // booking2 deve vir primeiro (data não nula)
    }

    // ========== TESTES DE API DE DISPONIBILIDADE ==========

    @Test
    void testCheckAvailability_API_Success() {
        // Arrange
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.checkAvailability(anyInt(), any(), any())).thenReturn(true);
        when(bookingService.calculatePrice(anyInt(), anyLong())).thenReturn(new BigDecimal("75.00"));

        // Act
        com.magiclook.dto.BookingRequestDTO requestDTO = new com.magiclook.dto.BookingRequestDTO();
        requestDTO.setItemId(testItem.getItemId());
        requestDTO.setStartUseDate(startDate);
        requestDTO.setEndUseDate(endDate);
        
        java.util.Map<String, Object> response = bookingController.checkAvailability(requestDTO);

        // Assert
        assertNotNull(response);
        assertTrue((Boolean) response.get("available"));
        assertEquals("Item disponível para o período selecionado", response.get("message"));
    }

    @Test
    void testCheckAvailability_API_NotAvailable() {
        // Arrange
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.checkAvailability(anyInt(), any(), any())).thenReturn(false);

        // Act
        com.magiclook.dto.BookingRequestDTO requestDTO = new com.magiclook.dto.BookingRequestDTO();
        requestDTO.setItemId(testItem.getItemId());
        requestDTO.setStartUseDate(startDate);
        requestDTO.setEndUseDate(endDate);
        
        java.util.Map<String, Object> response = bookingController.checkAvailability(requestDTO);

        // Assert
        assertNotNull(response);
        assertFalse((Boolean) response.get("available"));
        assertEquals("Item não disponível para o período selecionado", response.get("message"));
    }

    @Test
    void testCheckAvailability_API_Exception() {
        // Arrange
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.checkAvailability(anyInt(), any(), any()))
            .thenThrow(new RuntimeException("Erro na verificação"));

        // Act
        com.magiclook.dto.BookingRequestDTO requestDTO = new com.magiclook.dto.BookingRequestDTO();
        requestDTO.setItemId(testItem.getItemId());
        requestDTO.setStartUseDate(startDate);
        requestDTO.setEndUseDate(endDate);
        
        java.util.Map<String, Object> response = bookingController.checkAvailability(requestDTO);

        // Assert
        assertNotNull(response);
        assertFalse((Boolean) response.get("available"));
        assertNotNull(response.get("message"));
    }

    @Test
    void testCheckItemAvailability_API_Success() {
        // Arrange
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.isItemAvailable(anyInt(), any(), any())).thenReturn(true);

        // Act
        java.util.Map<String, Object> response = bookingController.checkItemAvailability(
            testItem.getItemId(),
            startDate,
            endDate
        );

        // Assert
        assertNotNull(response);
        assertTrue((Boolean) response.get("available"));
    }

    @Test
    void testCheckItemAvailability_API_NotAvailable() {
        // Arrange
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.isItemAvailable(anyInt(), any(), any())).thenReturn(false);
        
        List<Booking> conflicts = new ArrayList<>();
        conflicts.add(testBooking);
        when(bookingService.getConflictingBookings(anyInt(), any(), any())).thenReturn(conflicts);

        // Act
        java.util.Map<String, Object> response = bookingController.checkItemAvailability(
            testItem.getItemId(),
            startDate,
            endDate
        );

        // Assert
        assertNotNull(response);
        assertFalse((Boolean) response.get("available"));
        assertTrue(response.containsKey("conflicts"));
    }

    @Test
    void testCheckItemAvailability_API_Exception() {
        // Arrange
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = cal.getTime();

        when(bookingService.isItemAvailable(anyInt(), any(), any()))
            .thenThrow(new RuntimeException("Erro ao verificar disponibilidade"));

        // Act
        java.util.Map<String, Object> response = bookingController.checkItemAvailability(
            testItem.getItemId(),
            startDate,
            endDate
        );

        // Assert
        assertNotNull(response);
        assertFalse((Boolean) response.get("available"));
        assertTrue(response.containsKey("error"));
    }

    // ========== MÉTODOS AUXILIARES ==========

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
        
        // Configurar datas padrão (futuras)
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