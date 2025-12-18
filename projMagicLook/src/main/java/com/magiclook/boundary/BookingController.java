package com.magiclook.boundary;

import com.magiclook.data.*;
import com.magiclook.dto.*;
import com.magiclook.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
@RequestMapping("/magiclook")
public class BookingController {
    
    // Constantes para strings repetidas
    private static final String SESSION_LOGGED_IN_USER = "loggedInUser";
    private static final String REDIRECT_LOGIN = "redirect:/magiclook/login";
    private static final String REDIRECT_DASHBOARD = "redirect:/magiclook/dashboard";
    private static final String VIEW_BOOKING_FORM = "booking/bookingForm";
    private static final String VIEW_MY_BOOKINGS = "booking/myBookings";
    private static final String VIEW_BOOKING_DETAILS = "booking/booking-details";
    private static final String VIEW_BOOKING_CONFIRMATION = "booking/bookingConfirmation";
    private static final String ATTR_ERROR = "error";
    private static final String ATTR_AVAILABLE = "available";
    private static final String ATTR_MESSAGE = "message";
    private static final String ATTR_ITEM = "item";
    private static final String ATTR_USER = "user";
    private static final String ATTR_BOOKING = "booking";
    private static final String ATTR_BOOKINGS = "bookings";
    private static final String ATTR_FILTER = "filter";
    private static final String ATTR_SEARCH = "search";
    
    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;
    
    // Injeção por construtor
    public BookingController(BookingService bookingService, ItemService itemService, UserService userService) {
        this.bookingService = bookingService;
        this.itemService = itemService;
        this.userService = userService;
    }
    
    // Show booking form for specific item
    @GetMapping("/booking/{itemId}")
    public String showBookingForm(@PathVariable Integer itemId, 
                                  HttpSession session, 
                                  Model model) {
        User user = (User) session.getAttribute(SESSION_LOGGED_IN_USER);
        
        if (user == null) {
            session.setAttribute("redirectAfterLogin", "/magiclook/booking/" + itemId);
            return REDIRECT_LOGIN;
        }
        
        Item item = itemService.getItemById(itemId);
        if (item == null) {
            return REDIRECT_DASHBOARD;
        }
        
        model.addAttribute(ATTR_ITEM, item);
        model.addAttribute(ATTR_USER, user);
        return VIEW_BOOKING_FORM;
    }
    
    // Process booking - usando @RequestParam
    @PostMapping("/booking/create")
    public String createBooking(
            @RequestParam("itemId") Integer itemId,
            @RequestParam("startUseDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startUseDate,
            @RequestParam("endUseDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endUseDate,
            HttpSession session,
            Model model) {
        
        User user = (User) session.getAttribute(SESSION_LOGGED_IN_USER);
        
        if (user == null) {
            return REDIRECT_LOGIN;
        }
        
        // Buscar item
        Item item = itemService.getItemById(itemId);
        if (item == null) {
            model.addAttribute(ATTR_ERROR, "Item não encontrado.");
            return REDIRECT_DASHBOARD;
        }
        
        // Verificar disponibilidade (usando Date)
        boolean isAvailable = bookingService.checkAvailability(itemId, startUseDate, endUseDate);
        
        if (!isAvailable) {
            model.addAttribute(ATTR_ERROR, "Item não disponível nas datas selecionadas. Por favor, escolha outras datas.");
            model.addAttribute(ATTR_ITEM, item);
            return VIEW_BOOKING_FORM;
        }
        
        // Validar datas
        if (startUseDate == null || endUseDate == null || endUseDate.before(startUseDate)) {
            model.addAttribute(ATTR_ERROR, "Datas inválidas. A data de fim deve ser após a data de início.");
            model.addAttribute(ATTR_ITEM, item);
            return VIEW_BOOKING_FORM;
        }
        
        // Validar que a data de início não é no passado
        if (startUseDate.before(new Date())) {
            model.addAttribute(ATTR_ERROR, "A data de início não pode ser no passado.");
            model.addAttribute(ATTR_ITEM, item);
            return VIEW_BOOKING_FORM;
        }
        
        try {
            // Usar o método existente do serviço
            BookingRequestDTO bookingRequest = new BookingRequestDTO();
            bookingRequest.setItemId(itemId);
            bookingRequest.setStartUseDate(startUseDate);
            bookingRequest.setEndUseDate(endUseDate);
            
            Booking booking = bookingService.createBooking(bookingRequest, user);
            
            // Redirecionar para confirmação
            return "redirect:/magiclook/booking/confirmation/" + booking.getBookingId();
            
        } catch (Exception e) {
            model.addAttribute(ATTR_ERROR, "Erro ao criar reserva: " + e.getMessage());
            model.addAttribute(ATTR_ITEM, item);
            return VIEW_BOOKING_FORM;
        }
    }
    
    // Show confirmation page
    @GetMapping("/booking/confirmation/{bookingId}")
    public String showConfirmation(@PathVariable UUID bookingId, HttpSession session, Model model) {
        User user = (User) session.getAttribute(SESSION_LOGGED_IN_USER);
        
        if (user == null) {
            return REDIRECT_LOGIN;
        }
        
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null || !booking.getUser().getUserId().equals(user.getUserId())) {
            return REDIRECT_DASHBOARD;
        }
        
        model.addAttribute(ATTR_BOOKING, booking);
        model.addAttribute(ATTR_USER, user);
        return VIEW_BOOKING_CONFIRMATION;
    }
    
    // Show user's bookings
    @GetMapping("/my-bookings")
    public String showMyBookings(HttpSession session, Model model,
                                @RequestParam(required = false) String filter,
                                @RequestParam(required = false) String search) {
        User user = (User) session.getAttribute(SESSION_LOGGED_IN_USER);
        
        if (user == null) {
            return REDIRECT_LOGIN;
        }
        
        List<Booking> bookings = bookingService.getUserBookings(user);
    
        // Garantir que bookings nunca seja null
        if (bookings == null) {
            bookings = new ArrayList<>();
        }
        
        // Ordenar por data de início decrescente (mais recentes primeiro)
        sortBookingsByStartDateDesc(bookings);
        
        // Aplicar filtro de status (Ativas/Passadas)
        bookings = applyFilter(bookings, filter);
        
        // Aplicar pesquisa por nome do item
        bookings = applySearch(bookings, search);
        
        model.addAttribute(ATTR_BOOKINGS, bookings);
        model.addAttribute(ATTR_FILTER, filter);
        model.addAttribute(ATTR_SEARCH, search);
        model.addAttribute(ATTR_USER, user);
        model.addAttribute("activePage", "/booking/myBookings");
        
        return VIEW_MY_BOOKINGS;
    }

    private void sortBookingsByStartDateDesc(List<Booking> bookings) {
        bookings.sort((b1, b2) -> {
            if (b1.getStartUseDate() == null && b2.getStartUseDate() == null) return 0;
            if (b1.getStartUseDate() == null) return 1;
            if (b2.getStartUseDate() == null) return -1;
            return b2.getStartUseDate().compareTo(b1.getStartUseDate());
        });
    }

    private List<Booking> applyFilter(List<Booking> bookings, String filter) {
        if (filter == null || filter.isEmpty()) {
            return bookings;
        }
        
        Date today = new Date();
        
        if ("active".equals(filter)) {
            // Reservas ativas: data de fim no futuro
            return bookings.stream()
                .filter(booking -> booking.getEndUseDate() != null && booking.getEndUseDate().after(today))
                .collect(java.util.stream.Collectors.toList());
        } else if ("past".equals(filter)) {
            // Reservas passadas: data de fim no passado
            return bookings.stream()
                .filter(booking -> booking.getEndUseDate() != null && booking.getEndUseDate().before(today))
                .collect(java.util.stream.Collectors.toList());
        }
        
        return bookings;
    }

    private List<Booking> applySearch(List<Booking> bookings, String search) {
        if (search == null || search.isEmpty()) {
            return bookings;
        }
        
        String searchLower = search.toLowerCase();
        return bookings.stream()
            .filter(booking -> booking.getItem() != null && 
                   booking.getItem().getName() != null &&
                   booking.getItem().getName().toLowerCase().contains(searchLower))
            .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/my-bookings/{id}")
    public String bookingDetails(@PathVariable String id, HttpSession session, Model model) {
        User user = (User) session.getAttribute(SESSION_LOGGED_IN_USER);
        if (user == null) {
            return REDIRECT_LOGIN;
        }
        
        // Buscar a reserva pelo ID
        Booking booking = bookingService.getBookingById(java.util.UUID.fromString(id));
        
        // Verificar se a reserva existe e pertence ao usuário
        if (booking == null || !booking.getUser().getUserId().equals(user.getUserId())) {
            return "redirect:/magiclook/bookings/my-bookings";
        }
        
        model.addAttribute(ATTR_BOOKING, booking);
        model.addAttribute(ATTR_USER, user);
        model.addAttribute("activePage", "myBookings");
        
        return VIEW_BOOKING_DETAILS;
    }
    
    // Check availability (AJAX endpoint) - para o formulário antigo
    @PostMapping("/booking/check-availability")
    @ResponseBody
    public Map<String, Object> checkAvailability(@RequestBody BookingRequestDTO bookingRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean available = bookingService.checkAvailability(
                bookingRequest.getItemId(),
                bookingRequest.getStartUseDate(),
                bookingRequest.getEndUseDate()
            );
            
            if (available) {
                long useDays = bookingRequest.getUseDays();
                java.math.BigDecimal price = bookingService.calculatePrice(bookingRequest.getItemId(), useDays);
                
                response.put(ATTR_AVAILABLE, true);
                response.put("useDays", useDays);
                response.put("totalPrice", price);
                response.put(ATTR_MESSAGE, "Item disponível para o período selecionado");
            } else {
                response.put(ATTR_AVAILABLE, false);
                response.put(ATTR_MESSAGE, "Item não disponível para o período selecionado");
            }
            
        } catch (Exception e) {
            response.put(ATTR_AVAILABLE, false);
            response.put(ATTR_MESSAGE, e.getMessage());
        }
        
        return response;
    }
    
    // Nova API para verificar disponibilidade em tempo real (AJAX)
    @GetMapping("/api/items/{itemId}/check")
    @ResponseBody
    public Map<String, Object> checkItemAvailability(
        @PathVariable Integer itemId,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Converter Date para LocalDate para usar os novos métodos
            LocalDate startDate = start.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
            LocalDate endDate = end.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
            
            // Usar o método do serviço
            boolean available = bookingService.isItemAvailable(itemId, startDate, endDate);
            response.put(ATTR_AVAILABLE, available);
            
            if (!available) {
                // Obter conflitos
                List<Booking> conflicts = bookingService.getConflictingBookings(itemId, startDate, endDate);
                List<Map<String, String>> conflictList = conflicts.stream()
                    .map(b -> {
                        Map<String, String> conflictMap = new HashMap<>();
                        conflictMap.put("start", b.getStartUseDate().toString());
                        conflictMap.put("end", b.getEndUseDate().toString());
                        return conflictMap;
                    })
                    .collect(java.util.stream.Collectors.toList());
                response.put("conflicts", conflictList);
            }
            
        } catch (Exception e) {
            response.put(ATTR_ERROR, e.getMessage());
            response.put(ATTR_AVAILABLE, false);
        }
        
        return response;
    }
}