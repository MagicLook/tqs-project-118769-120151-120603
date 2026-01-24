package com.magiclook.service;

import com.magiclook.data.*;
import com.magiclook.dto.BookingRequestDTO;
import com.magiclook.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final ItemSingleRepository itemSingleRepository;
    private final UserRepository userRepository;
        
    private static final String NOT_FOUND = "Item não encontrado";
    private static final String CONFIRMED = "CONFIRMED";
    private static final String CANCELLED = "CANCELLED";
    private static final String COMPLETED = "COMPLETED";

    // Lock global para todas as reservas
    private static final Object GLOBAL_BOOKING_LOCK = new Object();

    public BookingService(BookingRepository bookingRepository, ItemRepository itemRepository,
                         ItemSingleRepository itemSingleRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.itemSingleRepository = itemSingleRepository;
        this.userRepository = userRepository;
    }
    
    public Booking createBooking(BookingRequestDTO bookingRequest, User user) {
        // Usar lock global para evitar qualquer concorrência (mais seguro)
        synchronized (GLOBAL_BOOKING_LOCK) {
            return doCreateBooking(bookingRequest, user);
        }
    }
    
    private Booking doCreateBooking(BookingRequestDTO bookingRequest, User user) {
        // Validate dates
        if (!bookingRequest.isValidDates()) {
            throw new IllegalArgumentException("Datas inválidas");
        }
        
        // Buscar o item
        Item item = itemRepository.findById(bookingRequest.getItemId())
            .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND));
        
        // Verificar utilizador autenticado e obter a entidade atualizada
        if (user == null) {
            throw new IllegalArgumentException("Utilizador não autenticado");
        }

        // IMPORTANTE: Validar que o user existe na BD
        User currentUser = userRepository.findById(user.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Utilizador não encontrado na base de dados. Por favor, faça logout e login novamente."));
        
        // Calcular datas
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(bookingRequest.getStartUseDate());
        calendar.add(Calendar.DAY_OF_MONTH, -1); // 1 dia antes para pickup
        Date pickupDate = calendar.getTime();
        
        Date startUseDate = bookingRequest.getStartUseDate();
        Date endUseDate = bookingRequest.getEndUseDate();
        
        calendar.setTime(endUseDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1); // 1 dia depois para devolução
        Date returnDate = calendar.getTime();
        
        // Calcular dias e preço
        long useDays = bookingRequest.getUseDays();
        BigDecimal totalPrice = item.getPriceRent().multiply(BigDecimal.valueOf(useDays));
        
        // Encontrar ItemSingle disponível PARA AS DATAS
        ItemSingle availableItemSingle = findAvailableItemSingleForDates(
            bookingRequest.getItemId(), 
            bookingRequest.getSize(),
            pickupDate, 
            startUseDate, 
            endUseDate, 
            returnDate
        );
        
        if (availableItemSingle == null) {
            String errorMsg = bookingRequest.getSize() != null && !bookingRequest.getSize().isEmpty()
                ? "Nenhuma unidade disponível para o tamanho " + bookingRequest.getSize() + " nas datas selecionadas"
                : "Nenhuma unidade disponível para as datas selecionadas";
            throw new IllegalStateException(errorMsg);
        }
        
        // Verificar se o ItemSingle está disponível fisicamente (não em manutenção)
        if (!ItemSingle.STATE_AVAILABLE.equals(availableItemSingle.getState())) {
            throw new IllegalStateException("Item está em manutenção e não está disponível");
        }
        
        // Criar reserva
        Booking booking = new Booking();
        booking.setPickupDate(pickupDate);
        booking.setStartUseDate(startUseDate);
        booking.setEndUseDate(endUseDate);
        booking.setReturnDate(returnDate);
        booking.setTotalDays((int) useDays);
        booking.setTotalPrice(totalPrice);
        booking.setState(CONFIRMED);
        booking.setItem(item);
        booking.setItemSingle(availableItemSingle);
        booking.setUser(currentUser);
        booking.setCreatedAt(new Date());
        
        // IMPORTANTE: NÃO alteramos o estado do ItemSingle!
        // A disponibilidade é determinada pelas reservas, não pelo estado
        
        // Salvar booking
        return bookingRepository.save(booking);
    }
    
    // Adicionar método createBookingWithSize para compatibilidade
    public Booking createBookingWithSize(BookingRequestDTO bookingRequest, User user) {
        return createBooking(bookingRequest, user);
    }
    
    private ItemSingle findAvailableItemSingleForDates(Integer itemId, String size, 
                                                      Date pickupDate, Date startUseDate, 
                                                      Date endUseDate, Date returnDate) {
        
        // Buscar todos ItemSingles do item que estão fisicamente disponíveis
        List<ItemSingle> itemSingles = itemSingleRepository.findByItem_ItemId(itemId)
            .stream()
            .filter(is -> ItemSingle.STATE_AVAILABLE.equals(is.getState())) // Apenas fisicamente disponíveis
            .filter(is -> size == null || size.isEmpty() || size.equals(is.getSize())) // Filtrar por tamanho
            .toList();
        
        if (itemSingles.isEmpty()) {
            return null;
        }
        
        // Verificar qual não tem reservas conflitantes
        for (ItemSingle itemSingle : itemSingles) {
            Long overlapping = bookingRepository.countOverlappingBookingsForItemSingle(
                itemSingle.getId(), pickupDate, startUseDate, endUseDate, returnDate);
            
            if (overlapping == 0) {
                return itemSingle;
            }
        }
        
        return null;
    }
    
    public boolean checkAvailability(Integer itemId, Date startUseDate, Date endUseDate) {
        return checkAvailabilityWithSize(itemId, null, startUseDate, endUseDate);
    }
    
    public boolean checkAvailabilityWithSize(Integer itemId, String size, Date startUseDate, Date endUseDate) {
        // Usar lock global para evitar concorrência durante verificação
        synchronized (GLOBAL_BOOKING_LOCK) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startUseDate);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            Date pickupDate = calendar.getTime();
            
            calendar.setTime(endUseDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date returnDate = calendar.getTime();
            
            // Buscar ItemSingles fisicamente disponíveis
            List<ItemSingle> availableItemSingles = itemSingleRepository.findByItem_ItemId(itemId)
                .stream()
                .filter(is -> ItemSingle.STATE_AVAILABLE.equals(is.getState()))
                .filter(is -> size == null || size.isEmpty() || size.equals(is.getSize()))
                .toList();
            
            if (availableItemSingles.isEmpty()) {
                return false;
            }
            
            // Verificar se algum não tem conflitos
            for (ItemSingle itemSingle : availableItemSingles) {
                Long overlapping = bookingRepository.countOverlappingBookingsForItemSingle(
                    itemSingle.getId(), pickupDate, startUseDate, endUseDate, returnDate);
                
                if (overlapping == 0) {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    public List<Booking> getUserBookings(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Utilizador não autenticado");
        }
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public BigDecimal calculatePrice(Integer itemId, long useDays) {
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND));
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
    
    public List<Booking> getConflictingBookingsBySize(Integer itemId, LocalDate startUseDate, LocalDate endUseDate) {
        // Return conflicts for the item
        return getConflictingBookings(itemId, startUseDate, endUseDate);
    }
    
    public void saveBooking(Booking booking) {
        bookingRepository.save(booking);
    }
    
    public Booking createSimpleBooking(Integer itemId, LocalDate startUseDate, LocalDate endUseDate, User user) {
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND));
        
        if (!isItemAvailable(itemId, startUseDate, endUseDate)) {
            throw new IllegalStateException("Item não disponível nas datas selecionadas");
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
        ItemSingle availableItemSingle = findAvailableItemSingleForDates(
            itemId, null, pickupDate, startDate, endDate, laundryDate);
        
        if (availableItemSingle == null) {
            throw new IllegalStateException("Nenhuma unidade disponível para as datas selecionadas");
        }
        
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
        booking.setState(CONFIRMED);
        booking.setItem(item);
        booking.setItemSingle(availableItemSingle);
        booking.setUser(user);
        booking.setCreatedAt(new Date());
        
        return bookingRepository.save(booking);
    }
    
    public boolean checkItemAvailability(Integer itemId, LocalDate start, LocalDate end) {
        return isItemAvailable(itemId, start, end);
    }

    public String getCurrentBookingState(Booking booking) {
        Date now = new Date();
        
        if (booking.getState() != null && 
            (booking.getState().equals(CANCELLED) || booking.getState().equals(COMPLETED))) {
            return booking.getState();
        }
        
        // Determinar estado baseado nas datas
        if (now.before(booking.getStartUseDate())) {
            return CONFIRMED;
        } else if (now.after(booking.getEndUseDate())) {
            // Verificar se já passou a data de devolução sem devolver
            if (now.after(booking.getReturnDate()) && !"RETURNED".equals(booking.getState())) {
                return "OVERDUE";
            } else {
                return COMPLETED;
            }
        } else {
            return "ACTIVE";
        }
    }

    /**
     * Calculate refund percentage and amount for a given booking based on time to start date.
     * Rules (applied in order):
     *  - >= 30 days before start: 100%
     *  - >= 15 days and < 30 days: 50%
     *  - >= 48 hours and < 15 days: 25%
     *  - < 48 hours: 0%
     */
    public com.magiclook.dto.RefundInfoDTO getRefundInfo(Booking booking) {
        if (booking == null || booking.getStartUseDate() == null || booking.getTotalPrice() == null) {
            return new com.magiclook.dto.RefundInfoDTO(0, java.math.BigDecimal.ZERO);
        }

        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault());
        java.time.ZonedDateTime start = booking.getStartUseDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().atStartOfDay(java.time.ZoneId.systemDefault());

        long hours = java.time.Duration.between(now, start).toHours();

        int percent;
        if (hours >= 30L * 24L) { // >= 30 days
            percent = 100;
        } else if (hours >= 15L * 24L) { // >= 15 days
            percent = 50;
        } else if (hours >= 48L) { // >= 48 hours
            percent = 25;
        } else {
            percent = 0;
        }

        java.math.BigDecimal amount = booking.getTotalPrice()
            .multiply(java.math.BigDecimal.valueOf(percent))
            .divide(java.math.BigDecimal.valueOf(100))
            .setScale(2, java.math.RoundingMode.HALF_UP);

        return new com.magiclook.dto.RefundInfoDTO(percent, amount);
    }

    /**
     * Cancel a booking: set state to CANCELLED and persist. Returns the refund info computed.
     */
    public com.magiclook.dto.RefundInfoDTO cancelBooking(Booking booking) {
        if (booking == null) throw new IllegalArgumentException("Reserva inexistente");

        // If already cancelled or completed, no-op
        if (CANCELLED.equals(booking.getState()) || COMPLETED.equals(booking.getState())) {
            return getRefundInfo(booking);
        }

        com.magiclook.dto.RefundInfoDTO info = getRefundInfo(booking);

        booking.setState(CANCELLED);
        bookingRepository.save(booking);

        return info;
    }
    
    // Método para obter tamanhos disponíveis para um item
    public List<String> getAvailableSizesForItem(Integer itemId) {
        return itemSingleRepository.findByItem_ItemId(itemId)
            .stream()
            .filter(is -> "AVAILABLE".equals(is.getState()))
            .map(ItemSingle::getSize)
            .distinct()
            .sorted()
            .toList();
    }
    
    // Método para obter contagem por tamanho
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
    
    public Map<String, Integer> getSizeAvailabilityForDates(Integer itemId, Date startUseDate, Date endUseDate) {
        Map<String, Integer> availability = new HashMap<>();
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startUseDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date pickupDate = calendar.getTime();
        
        calendar.setTime(endUseDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date returnDate = calendar.getTime();
        
        // Buscar todos ItemSingles fisicamente disponíveis
        List<ItemSingle> allItemSingles = itemSingleRepository.findByItem_ItemId(itemId)
            .stream()
            .filter(is -> ItemSingle.STATE_AVAILABLE.equals(is.getState()))
            .toList();
        
        // Para cada tamanho, contar quantos não têm conflitos
        for (ItemSingle itemSingle : allItemSingles) {
            String size = itemSingle.getSize() != null ? itemSingle.getSize() : "Único";
            
            Long overlapping = bookingRepository.countOverlappingBookingsForItemSingle(
                itemSingle.getId(), pickupDate, startUseDate, endUseDate, returnDate);
            
            if (overlapping == 0) {
                availability.put(size, availability.getOrDefault(size, 0) + 1);
            }
        }
        
        return availability;
    }
}