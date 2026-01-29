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

import io.micrometer.core.annotation.Timed;

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
    private static final String REDIRECT_MY_BOOKINGS = "redirect:/magiclook/my-bookings";
    private static final String BOOKING_STATUS_CONFIRMED = "CONFIRMED";
    private static final String BOOKING_STATUS_COMPLETED = "COMPLETED";
    private static final String ATTR_CAN_CANCEL = "canCancel";

    private final BookingService bookingService;
    private final ItemService itemService;

    // Injeção por construtor
    public BookingController(BookingService bookingService, ItemService itemService) {
        this.bookingService = bookingService;
        this.itemService = itemService;
    }

    // Show booking form for specific item
    @GetMapping("/booking/{itemId}")
    @Timed(value = "request.reservation", histogram = true, description = "Reservation form latency", extraTags = {
            "slo", "reservation", "operation", "showForm" })
    public String showBookingForm(@PathVariable Integer itemId,
            HttpSession session,
            Model model) {
        User user = (User) session.getAttribute(SESSION_LOGGED_IN_USER);

        if (user == null) {
            session.setAttribute("redirectAfterLogin", "/magiclook/booking/" + itemId);
            return REDIRECT_LOGIN;
        }

        Item item = itemService.getItemById(itemId).orElse(null);
        if (item == null) {
            return REDIRECT_DASHBOARD;
        }

        // Obter tamanhos disponíveis
        List<String> availableSizes = bookingService.getAvailableSizesForItem(itemId);

        // Obter contagem por tamanho
        Map<String, Integer> sizeAvailability = bookingService.getSizeAvailabilityCount(itemId);

        model.addAttribute(ATTR_ITEM, item);
        model.addAttribute(ATTR_USER, user);
        model.addAttribute("availableSizes", availableSizes);
        model.addAttribute("sizeAvailability", sizeAvailability);
        return VIEW_BOOKING_FORM;
    }

    // Process booking - usando @RequestParam
    @PostMapping("/booking/create")
    @Timed(value = "request.reservation", histogram = true, description = "Reservation creation latency", extraTags = {
            "slo", "reservation", "operation", "create" })
    public String createBooking(
            @RequestParam("itemId") Integer itemId,
            @RequestParam(value = "size", required = false) String size,
            @RequestParam("startUseDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startUseDate,
            @RequestParam("endUseDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endUseDate,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute(SESSION_LOGGED_IN_USER);

        if (user == null) {
            return REDIRECT_LOGIN;
        }

        // Buscar item
        Item item = itemService.getItemById(itemId).orElse(null);
        if (item == null) {
            model.addAttribute(ATTR_ERROR, "Item não encontrado.");
            return REDIRECT_DASHBOARD;
        }

        // Obter tamanhos disponíveis para mostrar novamente em caso de erro
        List<String> availableSizes = bookingService.getAvailableSizesForItem(itemId);
        Map<String, Integer> sizeAvailability = bookingService.getSizeAvailabilityCount(itemId);
        model.addAttribute("availableSizes", availableSizes);
        model.addAttribute("sizeAvailability", sizeAvailability);

        // Validar datas
        if (startUseDate == null || endUseDate == null || endUseDate.before(startUseDate)) {
            model.addAttribute(ATTR_ERROR, "Datas inválidas. A data de fim deve ser após a data de início.");
            model.addAttribute(ATTR_ITEM, item);
            return VIEW_BOOKING_FORM;
        }

        // Validar que a data de início não é no passado (comparar por data, ignorando
        // hora)
        java.time.LocalDate startLocal = startUseDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        java.time.LocalDate todayLocal = java.time.LocalDate.now(ZoneId.systemDefault());
        if (startLocal.isBefore(todayLocal)) {
            model.addAttribute(ATTR_ERROR, "A data de início não pode ser no passado.");
            model.addAttribute(ATTR_ITEM, item);
            return VIEW_BOOKING_FORM;
        }

        try {
            // Criar DTO para passar para o serviço
            BookingRequestDTO bookingRequest = new BookingRequestDTO();
            bookingRequest.setItemId(itemId);
            bookingRequest.setSize(size);
            bookingRequest.setStartUseDate(startUseDate);
            bookingRequest.setEndUseDate(endUseDate);

            // Verificar disponibilidade explicitamente antes de criar reserva
            boolean available = bookingService.checkAvailabilityWithSize(itemId, size, startUseDate, endUseDate);
            if (!available) {
                String msg = (size != null && !size.isEmpty())
                        ? "Item não disponível nas datas selecionadas para o tamanho " + size
                        : "Item não disponível nas datas selecionadas";
                model.addAttribute(ATTR_ERROR, msg);
                model.addAttribute(ATTR_ITEM, item);
                return VIEW_BOOKING_FORM;
            }

            // Usar o método do serviço - irá criar a reserva
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
    @Timed(value = "request.reservation", histogram = true, description = "Reservation confirmation latency", extraTags = {
            "slo", "reservation", "operation", "confirmation" })
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
    @Timed(value = "request.reservation", histogram = true, description = "My bookings list latency", extraTags = {
            "slo", "reservation", "operation", "list" })
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

        // Atualizar estados antes de ordenar
        for (Booking booking : bookings) {
            String currentState = bookingService.getCurrentBookingState(booking);
            booking.setState(currentState);
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
        model.addAttribute("activePage", "myBookings");

        return VIEW_MY_BOOKINGS;
    }

    private void sortBookingsByStartDateDesc(List<Booking> bookings) {
        bookings.sort((b1, b2) -> {
            if (b1.getStartUseDate() == null && b2.getStartUseDate() == null)
                return 0;
            if (b1.getStartUseDate() == null)
                return 1;
            if (b2.getStartUseDate() == null)
                return -1;
            return b2.getStartUseDate().compareTo(b1.getStartUseDate());
        });
    }

    private List<Booking> applyFilter(List<Booking> bookings, String filter) {
        if (filter == null || filter.isEmpty()) {
            return bookings;
        }

        if ("active".equals(filter)) {
            // Reservas ativas: estado CONFIRMED ou ACTIVE
            return bookings.stream()
                    .filter(booking -> BOOKING_STATUS_CONFIRMED.equals(booking.getState()) ||
                            "ACTIVE".equals(booking.getState()))
                    .toList();
        } else if ("past".equals(filter)) {
            // Reservas passadas: estado COMPLETED ou OVERDUE
            return bookings.stream()
                    .filter(booking -> BOOKING_STATUS_COMPLETED.equals(booking.getState()) ||
                            "OVERDUE".equals(booking.getState()))
                    .toList();
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
                .toList();
    }

    @GetMapping("/my-bookings/{id}")
    @Timed(value = "request.reservation", histogram = true, description = "Booking details latency", extraTags = {
            "slo", "reservation", "operation", "details" })
    public String bookingDetails(@PathVariable String id, HttpSession session, Model model) {
        User user = (User) session.getAttribute(SESSION_LOGGED_IN_USER);
        if (user == null) {
            return REDIRECT_LOGIN;
        }
        // Buscar a reserva pelo ID (deixar IllegalArgumentException propagar se id
        // inválido)
        UUID bookingId = UUID.fromString(id);

        Booking booking = bookingService.getBookingById(bookingId);

        // Verificar se a reserva existe e pertence ao usuário
        if (booking == null || !booking.getUser().getUserId().equals(user.getUserId())) {
            return REDIRECT_MY_BOOKINGS;
        }

        // Atualizar estado da reserva
        String currentState = bookingService.getCurrentBookingState(booking);
        booking.setState(currentState);
        // Determine if user can cancel: only owner and only when CONFIRMED and start
        // date is in the future
        boolean canCancel = BOOKING_STATUS_CONFIRMED.equals(currentState)
                && booking.getStartUseDate().after(new java.util.Date());

        com.magiclook.dto.RefundInfoDTO refundInfo = bookingService.getRefundInfo(booking);

        // Transfer any flash message from session
        Object flashMsg = session.getAttribute(ATTR_MESSAGE);
        if (flashMsg != null) {
            model.addAttribute(ATTR_MESSAGE, flashMsg.toString());
            session.removeAttribute(ATTR_MESSAGE);
        }

        model.addAttribute(ATTR_BOOKING, booking);
        model.addAttribute(ATTR_USER, user);
        model.addAttribute("activePage", "myBookings");
        model.addAttribute(ATTR_CAN_CANCEL, canCancel);
        model.addAttribute("refundPercent", refundInfo.getPercent());
        model.addAttribute("refundAmount", refundInfo.getAmount());

        return VIEW_BOOKING_DETAILS;
    }

    // Endpoint to expose refund info (AJAX) and whether cancel is allowed
    @GetMapping("/my-bookings/{id}/cancel-info")
    @ResponseBody
    public Map<String, Object> cancelInfo(@PathVariable String id, HttpSession session) {
        Map<String, Object> resp = new HashMap<>();

        User user = (User) session.getAttribute(SESSION_LOGGED_IN_USER);
        Object staff = session.getAttribute("loggedInStaff");

        try {
            java.util.UUID bookingId = java.util.UUID.fromString(id);
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                resp.put(ATTR_CAN_CANCEL, false);
                resp.put(ATTR_MESSAGE, "Reserva não encontrada");
                return resp;
            }

            boolean allowed = false;
            // owner
            if (user != null && booking.getUser().getUserId().equals(user.getUserId())) {
                allowed = BOOKING_STATUS_CONFIRMED.equals(bookingService.getCurrentBookingState(booking))
                        && booking.getStartUseDate().after(new java.util.Date());
            }
            // staff may cancel
            if (!allowed && staff != null) {
                String state = bookingService.getCurrentBookingState(booking);
                allowed = !("CANCELLED".equals(state) || BOOKING_STATUS_COMPLETED.equals(state));
            }

            if (!allowed) {
                resp.put(ATTR_CAN_CANCEL, false);
                resp.put(ATTR_MESSAGE, "Cancelamento não permitido");
                return resp;
            }

            com.magiclook.dto.RefundInfoDTO info = bookingService.getRefundInfo(booking);
            resp.put(ATTR_CAN_CANCEL, true);
            resp.put("percent", info.getPercent());
            resp.put("amount", info.getAmount());
            return resp;

        } catch (Exception e) {
            resp.put(ATTR_CAN_CANCEL, false);
            resp.put(ATTR_MESSAGE, e.getMessage());
            return resp;
        }
    }

    @PostMapping("/my-bookings/{id}/cancel")
    public String cancelBooking(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute(SESSION_LOGGED_IN_USER);
        Object staff = session.getAttribute("loggedInStaff");

        try {
            java.util.UUID bookingId = java.util.UUID.fromString(id);
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                session.setAttribute(ATTR_MESSAGE, "Reserva não encontrada");
                return REDIRECT_MY_BOOKINGS;
            }

            boolean allowed = false;
            if (user != null && booking.getUser().getUserId().equals(user.getUserId())) {
                allowed = BOOKING_STATUS_CONFIRMED.equals(bookingService.getCurrentBookingState(booking))
                        && booking.getStartUseDate().after(new java.util.Date());
            }
            if (!allowed && staff != null) {
                String state = bookingService.getCurrentBookingState(booking);
                allowed = !("CANCELLED".equals(state) || BOOKING_STATUS_COMPLETED.equals(state));
            }

            if (!allowed) {
                session.setAttribute(ATTR_MESSAGE, "Cancelamento não permitido");
                return "redirect:/magiclook/my-bookings/" + id;
            }

            com.magiclook.dto.RefundInfoDTO info = bookingService.cancelBooking(booking);

            session.setAttribute(ATTR_MESSAGE,
                    "Reserva cancelada com sucesso. Reembolso: " + info.getAmount() + " (" + info.getPercent() + "%).");

            return "redirect:/magiclook/my-bookings/" + id;

        } catch (Exception e) {
            session.setAttribute(ATTR_MESSAGE, "Erro ao cancelar: " + e.getMessage());
            return REDIRECT_MY_BOOKINGS;
        }
    }

    // Check availability (AJAX endpoint) - para o formulário antigo
    @PostMapping("/booking/check-availability")
    @ResponseBody
    @Timed(value = "request.reservation", histogram = true, description = "Availability check latency", extraTags = {
            "slo", "reservation", "operation", "checkAvailability" })
    public Map<String, Object> checkAvailability(@RequestBody BookingRequestDTO bookingRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean available = bookingService.checkAvailabilityWithSize(
                    bookingRequest.getItemId(),
                    bookingRequest.getSize(),
                    bookingRequest.getStartUseDate(),
                    bookingRequest.getEndUseDate());

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
    @Timed(value = "request.reservation", histogram = true, description = "Item availability check latency", extraTags = {
            "slo", "reservation", "operation", "checkItemAvailability" })
    public Map<String, Object> checkItemAvailability(
            @PathVariable Integer itemId,
            @RequestParam(required = false) String size,
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

            boolean available;
            if (size == null || size.isEmpty()) {
                available = bookingService.isItemAvailable(itemId, startDate, endDate);
            } else {
                available = bookingService.isItemAvailableWithSize(itemId, size, startDate, endDate);
            }

            response.put(ATTR_AVAILABLE, available);

            if (!available) {
                // Obter conflitos
                List<Booking> conflicts = bookingService.getConflictingBookings(itemId, startDate, endDate);
                List<Map<String, String>> conflictList = conflicts.stream()
                        .map(b -> {
                            Map<String, String> conflictMap = new HashMap<>();
                            conflictMap.put("start", b.getStartUseDate().toString());
                            conflictMap.put("end", b.getEndUseDate().toString());
                            conflictMap.put("size", b.getItemSingle() != null ? b.getItemSingle().getSize() : "N/A");
                            return conflictMap;
                        })
                        .toList();
                response.put("conflicts", conflictList);
            }

        } catch (Exception e) {
            response.put(ATTR_ERROR, e.getMessage());
            response.put(ATTR_AVAILABLE, false);
        }

        return response;
    }

    // API endpoint simples para obter datas indisponíveis (para o calendário)
    @GetMapping("/api/availability")
    @ResponseBody
    public Map<String, Object> getUnavailableDates(@RequestParam Integer itemId,
            @RequestParam(required = false) String size) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Obter todas as reservas do item
            java.time.LocalDate today = java.time.LocalDate.now(ZoneId.systemDefault());
            java.time.LocalDate endDate = today.plusMonths(3); // Ver 3 meses à frente

            List<Booking> bookings = bookingService.getConflictingBookings(itemId, today, endDate);

            List<String> unavailableDates = new java.util.ArrayList<>();

            for (Booking booking : bookings) {
                // Se foi especificado um tamanho, filtrar apenas as reservas desse tamanho
                if (size != null && !size.isEmpty()) {
                    String bookingSize = booking.getItemSingle() != null ? booking.getItemSingle().getSize() : "";
                    if (!size.equalsIgnoreCase(bookingSize)) {
                        continue; // Saltar esta reserva se não é do tamanho solicitado
                    }
                }

                // Adicionar todas as datas do período de reserva como indisponíveis
                // INCLUINDO um dia antes (para busca) e um dia depois (para lavagem)
                java.time.LocalDate current = booking.getStartUseDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .minusDays(1); // Um dia antes para busca
                java.time.LocalDate end = booking.getEndUseDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .plusDays(1); // Um dia depois para lavagem

                while (!current.isAfter(end)) {
                    unavailableDates.add(current.toString());
                    current = current.plusDays(1);
                }
            }

            response.put("unavailableDates", unavailableDates);
        } catch (Exception e) {
            response.put("unavailableDates", new java.util.ArrayList<>());
            response.put(ATTR_ERROR, e.getMessage());
        }

        return response;
    }
}