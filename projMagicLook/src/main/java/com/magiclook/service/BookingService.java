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
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private ItemSingleRepository itemSingleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Booking createBooking(BookingRequestDTO bookingRequest, User user) {
        // Validate dates
        if (!bookingRequest.isValidDates()) {
            throw new IllegalArgumentException("Datas inválidas");
        }
        
        // ENCONTRAR UM ItemSingle DISPONÍVEL (não o Item)
        List<ItemSingle> availableItemSingles = itemSingleRepository
            .findByItem_ItemId(bookingRequest.getItemId())
            .stream()
            .filter(is -> "AVAILABLE".equals(is.getState()))
            .filter(is -> bookingRequest.getSize() == null || 
                         bookingRequest.getSize().isEmpty() || 
                         bookingRequest.getSize().equals(is.getSize()))
            .collect(Collectors.toList());
        
        if (availableItemSingles.isEmpty()) {
            throw new RuntimeException("Nenhuma unidade disponível para o item selecionado");
        }
        
        // Verificar disponibilidade para as datas
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(bookingRequest.getStartUseDate());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date pickupDate = calendar.getTime();
        
        Date startUseDate = bookingRequest.getStartUseDate();
        Date endUseDate = bookingRequest.getEndUseDate();
        
        calendar.setTime(endUseDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date returnDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date laundryDate = calendar.getTime();
        
        // Encontrar um ItemSingle sem conflitos
        ItemSingle selectedItemSingle = null;
        
        for (ItemSingle itemSingle : availableItemSingles) {
            Long overlapping = bookingRepository.countOverlappingBookingsForItemSingle(
                itemSingle.getId(), pickupDate, startUseDate, endUseDate, laundryDate);
            
            if (overlapping == 0) {
                selectedItemSingle = itemSingle;
                break;
            }
        }
        
        if (selectedItemSingle == null) {
            throw new RuntimeException("Nenhuma unidade disponível para as datas selecionadas");
        }
        
        // Calcular preço
        long useDays = bookingRequest.getUseDays();
        BigDecimal totalPrice = selectedItemSingle.getItem().getPriceRent()
            .multiply(BigDecimal.valueOf(useDays));
        
        // Criar booking
        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID());
        booking.setPickupDate(pickupDate);
        booking.setStartUseDate(startUseDate);
        booking.setEndUseDate(endUseDate);
        booking.setReturnDate(returnDate);
        booking.setTotalDays((int) useDays);
        booking.setTotalPrice(totalPrice);
        booking.setState("CONFIRMED");
        booking.setItem(selectedItemSingle.getItem()); // Item para referência
        booking.setItemSingle(selectedItemSingle); // ItemSingle específico
        booking.setUser(user);
        booking.setCreatedAt(new Date());
        
        // Atualizar estado do ItemSingle (não do Item!)
        selectedItemSingle.setState("RESERVED");
        itemSingleRepository.save(selectedItemSingle);
        
        // NÃO atualize o estado do Item
        // O Item deve permanecer disponível se houver outros ItemSingle disponíveis
        
        return bookingRepository.save(booking);
    }
    
    public Booking createBookingWithSize(BookingRequestDTO bookingRequest, User user) {
        // Alias for createBooking with size
        return createBooking(bookingRequest, user);
    }
    
    private ItemSingle findAvailableItemSingle(Integer itemId, String size, 
                                             Date pickupDate, Date startUseDate, 
                                             Date endUseDate, Date laundryDate) {
        List<ItemSingle> itemSingles;
        
        if (size != null && !size.isEmpty()) {
            // Buscar ItemSingle por tamanho específico (estado AVAILABLE)
            itemSingles = itemSingleRepository.findByItem_ItemId(itemId)
                .stream()
                .filter(is -> size.equals(is.getSize()) && "AVAILABLE".equals(is.getState()))
                .toList();
        } else {
            // Buscar qualquer ItemSingle disponível
            itemSingles = itemSingleRepository.findByItem_ItemId(itemId)
                .stream()
                .filter(is -> "AVAILABLE".equals(is.getState()))
                .toList();
        }
        
        if (itemSingles.isEmpty()) {
            return null;
        }
        
        // Verificar quais não têm reservas conflitantes
        for (ItemSingle itemSingle : itemSingles) {
            Long overlapping = countOverlappingBookingsForItemSingle(
                itemSingle.getId(), pickupDate, startUseDate, endUseDate, laundryDate);
            
            if (overlapping == 0) {
                return itemSingle;
            }
        }
        
        return null;
    }
    
    // Método auxiliar para contar reservas conflitantes para um ItemSingle
    private Long countOverlappingBookingsForItemSingle(UUID itemSingleId, Date pickupDate, 
                                                      Date startUseDate, Date endUseDate, Date laundryDate) {
        // Usando query JPQL para contar reservas conflitantes
        return bookingRepository.countOverlappingBookingsForItemSingle(
            itemSingleId, pickupDate, startUseDate, endUseDate, laundryDate);
    }
    
    public List<Booking> getUserBookings(User user) {
        if (user == null) {
            throw new RuntimeException("Utilizador não autenticado");
        }
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public boolean checkAvailability(Integer itemId, Date startUseDate, Date endUseDate) {
        // For backward compatibility, this method doesn't consider size
        // It checks if there's any available ItemSingle for the given dates
        return checkAvailabilityWithSize(itemId, null, startUseDate, endUseDate);
    }
    
    public boolean checkAvailabilityWithSize(Integer itemId, String size, Date startUseDate, Date endUseDate) {
        // Buscar ItemSingles disponíveis
        List<ItemSingle> availableItemSingles = itemSingleRepository
            .findByItem_ItemId(itemId)
            .stream()
            .filter(is -> "AVAILABLE".equals(is.getState()))
            .filter(is -> size == null || size.isEmpty() || size.equals(is.getSize()))
            .collect(Collectors.toList());
        
        if (availableItemSingles.isEmpty()) {
            return false;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startUseDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date pickupDate = calendar.getTime();
        
        calendar.setTime(endUseDate);
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        Date laundryDate = calendar.getTime();
        
        // Verificar se algum ItemSingle está disponível para as datas
        for (ItemSingle itemSingle : availableItemSingles) {
            Long overlapping = bookingRepository.countOverlappingBookingsForItemSingle(
                itemSingle.getId(), pickupDate, startUseDate, endUseDate, laundryDate);
            
            if (overlapping == 0) {
                return true;
            }
        }
        
        return false;
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
        
        // Usar o método existente que verifica disponibilidade (sem tamanho)
        return checkAvailability(itemId, startDate, endDate);
    }
    
    public boolean isItemAvailableWithSize(Integer itemId, String size, LocalDate startUseDate, LocalDate endUseDate) {
        // Converter LocalDate para Date
        Date startDate = Date.from(startUseDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endUseDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        // Usar o método que considera tamanho
        return checkAvailabilityWithSize(itemId, size, startDate, endDate);
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
            
            // Chamar o método do repositório (para o item em geral)
            return bookingRepository.findOverlappingBookings(
                itemId, pickupDate, startDate, endDate, laundryDate
            );
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public List<Booking> getConflictingBookingsBySize(Integer itemId, String size, LocalDate startUseDate, LocalDate endUseDate) {
        // For now, return conflicts for the item (size filtering could be added later)
        return getConflictingBookings(itemId, startUseDate, endUseDate);
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
        
        // Encontrar ItemSingle disponível (qualquer tamanho)
        ItemSingle availableItemSingle = findAvailableItemSingle(
            itemId, null, pickupDate, startDate, endDate, laundryDate);
        
        if (availableItemSingle == null) {
            throw new RuntimeException("Nenhuma unidade disponível para as datas selecionadas");
        }
        
        // Calcular dias e preço
        long useDays = ChronoUnit.DAYS.between(startUseDate, endUseDate) + 1;
        BigDecimal totalPrice = item.getPriceRent().multiply(BigDecimal.valueOf(useDays));
        
        // Criar booking
        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID());
        booking.setPickupDate(pickupDate);
        booking.setStartUseDate(startDate);
        booking.setEndUseDate(endDate);
        booking.setReturnDate(returnDate);
        booking.setTotalDays((int) useDays);
        booking.setTotalPrice(totalPrice);
        booking.setState("CONFIRMED");
        booking.setItem(item);
        booking.setItemSingle(availableItemSingle);
        booking.setUser(user);
        booking.setCreatedAt(new Date());
        
        // Atualizar estado do ItemSingle
        availableItemSingle.setState("RESERVED");
        itemSingleRepository.save(availableItemSingle);
        
        return bookingRepository.save(booking);
    }
    
    public boolean checkItemAvailability(Integer itemId, LocalDate start, LocalDate end) {
        return isItemAvailable(itemId, start, end);
    }

    public String getCurrentBookingState(Booking booking) {
        Date now = new Date();
        
        if (booking.getState() != null && 
            (booking.getState().equals("CANCELLED") || booking.getState().equals("COMPLETED"))) {
            return booking.getState();
        }
        
        // Determinar estado baseado nas datas
        if (now.before(booking.getStartUseDate())) {
            return "CONFIRMED";
        } else if (now.after(booking.getEndUseDate())) {
            // Verificar se já passou a data de devolução sem devolver
            if (now.after(booking.getReturnDate()) && !"RETURNED".equals(booking.getState())) {
                return "OVERDUE";
            } else {
                return "COMPLETED";
            }
        } else {
            return "ACTIVE";
        }
    }
    
    // Novo método para obter tamanhos disponíveis para um item
    public List<String> getAvailableSizesForItem(Integer itemId) {
        return itemSingleRepository.findByItem_ItemId(itemId)
            .stream()
            .filter(is -> "AVAILABLE".equals(is.getState()))
            .map(ItemSingle::getSize)
            .distinct()
            .sorted()
            .toList();
    }
    
    // Novo método para obter contagem por tamanho
    public Map<String, Integer> getSizeAvailabilityCount(Integer itemId) {
        Map<String, Integer> sizeCount = new HashMap<>();
        
        List<ItemSingle> itemSingles = itemSingleRepository.findByItem_ItemId(itemId)
            .stream()
            .filter(is -> "AVAILABLE".equals(is.getState()))
            .toList();
        
        for (ItemSingle itemSingle : itemSingles) {
            String size = itemSingle.getSize() != null ? itemSingle.getSize() : "Único";
            sizeCount.put(size, sizeCount.getOrDefault(size, 0) + 1);
        }
        
        return sizeCount;
    }
}