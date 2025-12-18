package com.magiclook.boundary;

import com.magiclook.data.*;
import com.magiclook.dto.*;
import com.magiclook.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/magiclook")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private UserService userService;
    
    // Show booking form for specific item
    @GetMapping("/booking/{itemId}")
    public String showBookingForm(@PathVariable Integer itemId, 
                                  HttpSession session, 
                                  Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user == null) {
            session.setAttribute("redirectAfterLogin", "/magiclook/booking/" + itemId);
            return "redirect:/magiclook/login";
        }
        
        Item item = itemService.getItemById(itemId);
        if (item == null) {
            return "redirect:/magiclook/dashboard";
        }
        
        model.addAttribute("item", item);
        model.addAttribute("user", user);
        return "booking/bookingForm";
    }
    
    // Process booking - usando @RequestParam
    @PostMapping("/booking/create")
    public String createBooking(
            @RequestParam("itemId") Integer itemId,
            @RequestParam("startUseDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startUseDate,
            @RequestParam("endUseDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endUseDate,
            HttpSession session,
            Model model) {
        
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user == null) {
            return "redirect:/magiclook/login";
        }
        
        // Buscar item
        Item item = itemService.getItemById(itemId);
        if (item == null) {
            model.addAttribute("error", "Item não encontrado.");
            return "redirect:/magiclook/dashboard";
        }
        
        // Verificar disponibilidade (usando Date)
        boolean isAvailable = bookingService.checkAvailability(itemId, startUseDate, endUseDate);
        
        if (!isAvailable) {
            model.addAttribute("error", "Item não disponível nas datas selecionadas. Por favor, escolha outras datas.");
            model.addAttribute("item", item);
            return "booking/bookingForm";
        }
        
        // Validar datas
        if (startUseDate == null || endUseDate == null || endUseDate.before(startUseDate)) {
            model.addAttribute("error", "Datas inválidas. A data de fim deve ser após a data de início.");
            model.addAttribute("item", item);
            return "booking/bookingForm";
        }
        
        // Validar que a data de início não é no passado
        if (startUseDate.before(new Date())) {
            model.addAttribute("error", "A data de início não pode ser no passado.");
            model.addAttribute("item", item);
            return "booking/bookingForm";
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
            model.addAttribute("error", "Erro ao criar reserva: " + e.getMessage());
            model.addAttribute("item", item);
            return "booking/bookingForm";
        }
    }
    
    // Show confirmation page
    @GetMapping("/booking/confirmation/{bookingId}")
    public String showConfirmation(@PathVariable UUID bookingId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user == null) {
            return "redirect:/magiclook/login";
        }
        
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null || !booking.getUser().getUserId().equals(user.getUserId())) {
            return "redirect:/magiclook/dashboard";
        }
        
        model.addAttribute("booking", booking);
        model.addAttribute("user", user);
        return "booking/bookingConfirmation";
    }
    
    // Show user's bookings
    @GetMapping("/my-bookings")
    public String showMyBookings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user == null) {
            return "redirect:/magiclook/login";
        }
        
        List<Booking> bookings = bookingService.getUserBookings(user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("user", user);
        model.addAttribute("activePage", "myBookings");
        return "booking/myBookings";
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
                
                response.put("available", true);
                response.put("useDays", useDays);
                response.put("totalPrice", price);
                response.put("message", "Item disponível para o período selecionado");
            } else {
                response.put("available", false);
                response.put("message", "Item não disponível para o período selecionado");
            }
            
        } catch (Exception e) {
            response.put("available", false);
            response.put("message", e.getMessage());
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
            response.put("available", available);
            
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
                    .collect(Collectors.toList());
                response.put("conflicts", conflictList);
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("available", false);
        }
        
        return response;
    }
}