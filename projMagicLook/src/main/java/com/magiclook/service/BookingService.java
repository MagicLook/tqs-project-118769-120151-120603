package com.magiclook.service;

import com.magiclook.data.*;
import com.magiclook.dto.BookingRequestDTO;
import com.magiclook.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Booking createBooking(BookingRequestDTO bookingRequest, User user) {
        // Validate dates
        if (!bookingRequest.isValidDates()) {
            throw new IllegalArgumentException("Datas inválidas");
        }
        
        // Check if item exists and is available
        Item item = itemRepository.findById(bookingRequest.getItemId())
            .orElseThrow(() -> new RuntimeException("Item não encontrado"));
        
        // Check if user is authenticated
        if (user == null) {
            throw new RuntimeException("Utilizador não autenticado");
        }
        
        // Calculate all dates (3-day minimum period)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(bookingRequest.getStartUseDate());
        calendar.add(Calendar.DAY_OF_MONTH, -1); // Pickup day
        Date pickupDate = calendar.getTime();
        
        Date startUseDate = bookingRequest.getStartUseDate();
        Date endUseDate = bookingRequest.getEndUseDate();
        
        calendar.setTime(endUseDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1); // Return day
        Date returnDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1); // Laundry day (next day after return)
        Date laundryDate = calendar.getTime();
        
        // Calculate use days and price
        long useDays = bookingRequest.getUseDays();
        BigDecimal totalPrice = item.getPriceRent().multiply(BigDecimal.valueOf(useDays));
        
        // Check availability for the entire period (including laundry day)
        Long overlappingBookings = bookingRepository.countOverlappingBookings(
            item.getItemId(),
            pickupDate,
            startUseDate,
            endUseDate,
            laundryDate
        );
        
        // For simplicity, if item has multiple units (itemSingles), we need to check each
        // For now, we'll consider item as single unit
        if (overlappingBookings > 0) {
            throw new RuntimeException("Item não disponível para o período solicitado. " +
                                     "Já existem reservas para estas datas.");
        }
        
        // Create booking
        Booking booking = new Booking();
        booking.setPickupDate(pickupDate);
        booking.setStartUseDate(startUseDate);
        booking.setEndUseDate(endUseDate);
        booking.setReturnDate(returnDate);
        booking.setTotalDays((int) useDays);
        booking.setTotalPrice(totalPrice);
        booking.setState("CONFIRMED");
        booking.setItem(item);
        booking.setUser(user);
        
        // Update item availability (simplified - in real scenario, would manage ItemSingle units)
        item.setAvailable(false);
        item.setNextAvailableDate(laundryDate);
        itemRepository.save(item);
        
        return bookingRepository.save(booking);
    }
    
    public List<Booking> getUserBookings(User user) {
        if (user == null) {
            throw new RuntimeException("Utilizador não autenticado");
        }
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public boolean checkAvailability(Integer itemId, Date startUseDate, Date endUseDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startUseDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date pickupDate = calendar.getTime();
        
        calendar.setTime(endUseDate);
        calendar.add(Calendar.DAY_OF_MONTH, 2); // Return + laundry day
        Date lastBlockedDate = calendar.getTime();
        
        Long overlapping = bookingRepository.countOverlappingBookings(
            itemId,
            pickupDate,
            startUseDate,
            endUseDate,
            lastBlockedDate
        );
        
        return overlapping == 0;
    }
    
    public BigDecimal calculatePrice(Integer itemId, long useDays) {
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item não encontrado"));
        return item.getPriceRent().multiply(BigDecimal.valueOf(useDays));
    }

    public Booking getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }
        
    public boolean isItemAvailable(Integer itemId, LocalDate startUseDate, LocalDate endUseDate) {
        // Converter LocalDate para Date
        Date startDate = Date.from(startUseDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endUseDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        // Usar o método existente que verifica disponibilidade
        return checkAvailability(itemId, startDate, endDate);
    }
    
    public List<Booking> getConflictingBookings(Integer itemId, LocalDate startUseDate, LocalDate endUseDate) {
        try {
            // Converter LocalDate para Date
            Date startDate = Date.from(startUseDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endUseDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            // Calcular pickupDate e returnDate (1 dia antes e 2 dias depois)
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_MONTH, -1); // pickup date
            Date pickupDate = calendar.getTime();
            
            calendar.setTime(endDate);
            calendar.add(Calendar.DAY_OF_MONTH, 2);
            Date laundryDate = calendar.getTime();
            
            // Chamar o método do repositório
            return bookingRepository.findOverlappingBookings(
                itemId, pickupDate, startDate, endDate, laundryDate
            );
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public void saveBooking(Booking booking) {
        bookingRepository.save(booking);
    }
    
    public Booking createSimpleBooking(Integer itemId, LocalDate startUseDate, LocalDate endUseDate, User user) {
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item não encontrado"));
        
        if (!isItemAvailable(itemId, startUseDate, endUseDate)) {
            throw new RuntimeException("Item não disponível nas datas selecionadas");
        }
        
        // Converter para Date
        Date startDate = Date.from(startUseDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endUseDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date pickupDate = calendar.getTime();
        
        calendar.setTime(endDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date returnDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date laundryDate = calendar.getTime();
        
        // Calcular dias e preço
        long useDays = ChronoUnit.DAYS.between(startUseDate, endUseDate) + 1;
        BigDecimal totalPrice = item.getPriceRent().multiply(BigDecimal.valueOf(useDays));
        
        // Criar booking
        Booking booking = new Booking();
        booking.setPickupDate(pickupDate);
        booking.setStartUseDate(startDate);
        booking.setEndUseDate(endDate);
        booking.setReturnDate(returnDate);
        booking.setTotalDays((int) useDays);
        booking.setTotalPrice(totalPrice);
        booking.setState("CONFIRMED");
        booking.setItem(item);
        booking.setUser(user);
        booking.setCreatedAt(new Date());
        
        return bookingRepository.save(booking);
    }
    
    public boolean checkItemAvailability(Integer itemId, LocalDate start, LocalDate end) {
        return isItemAvailable(itemId, start, end);
    }
}