package com.magiclook.service;

import com.magiclook.data.*;
import com.magiclook.dto.*;
import com.magiclook.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.*;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class StaffService {

    private static final Logger logger = LoggerFactory.getLogger(StaffService.class);
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final StaffRepository staffRepository;
    private final ShopRepository shopRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final ItemRepository itemRepository;
    private final ItemSingleRepository itemSingleRepository;
    private final NotificationRepository notificationRepository;
    private final BookingRepository bookingRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public StaffService(StaffRepository staffRepository, ItemRepository itemRepository, ShopRepository shopRepository,
            ItemTypeRepository itemTypeRepository, ItemSingleRepository itemSingleRepository,
            BookingRepository bookingRepository, NotificationRepository notificationRepository) {
        this.staffRepository = staffRepository;
        this.itemRepository = itemRepository;
        this.shopRepository = shopRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.itemSingleRepository = itemSingleRepository;
        this.bookingRepository = bookingRepository;
        this.notificationRepository = notificationRepository;
    }

    public String saveImage(MultipartFile image, Integer itemId) throws IOException {

        if (image == null || image.isEmpty()) {
            return null;
        }

        // Normalize target directory
        String normalizedDir = uploadDir.startsWith("/") ? uploadDir.substring(1) : uploadDir;

        // 1. Save to Source Directory (Persistence)
        Path srcStaticBase = Paths.get("src/main/resources/static").toAbsolutePath();
        Path srcUploadPath = srcStaticBase.resolve(normalizedDir);
        Files.createDirectories(srcUploadPath);

        // 2. Save to Target Directory (Runtime - immediate update)
        // Try to locate target/classes/static relative to project root
        Path targetStaticBase = Paths.get("target/classes/static").toAbsolutePath();
        Path targetUploadPath = targetStaticBase.resolve(normalizedDir);
        Files.createDirectories(targetUploadPath);

        String safeOriginal = image.getOriginalFilename() == null ? "file"
                : image.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
        String idPart = (itemId != null) ? String.valueOf(itemId) : UUID.randomUUID().toString().substring(0, 8);
        String fileName = String.format("item_%s_%s", idPart, safeOriginal);

        // Read bytes once to avoid stream exhaustion issues with large files
        byte[] imageBytes = image.getBytes();

        // Save to source
        Path srcFilePath = srcUploadPath.resolve(fileName);
        Files.write(srcFilePath, imageBytes);

        // Save to target
        Path targetFilePath = targetUploadPath.resolve(fileName);
        Files.write(targetFilePath, imageBytes);

        return "/" + normalizedDir.replace("\\", "/") + "/" + fileName;
    }

    public void updateItemImage(Integer itemId, String imagePath) {
        if (itemId == null || imagePath == null || imagePath.isBlank()) {
            return;
        }
        itemRepository.findById(itemId).ifPresent(item -> {
            item.setImagePath(imagePath);
            itemRepository.saveAndFlush(item);
        });
    }

    public void deleteItemSize(Integer itemId, String size) {
        itemSingleRepository.deleteByItem_ItemIdAndSize(itemId, size);

        if (itemSingleRepository.findByItem_ItemId(itemId).isEmpty()) {
            itemRepository.deleteById(itemId);
        }
    }

    public void updateItemSingle(UUID id, String size, String state, String damageReason) {
        itemSingleRepository.findById(id).ifPresent(single -> {
            boolean changed = false;
            if (size != null && !size.isBlank()) {
                single.setSize(size);
                changed = true;
            }
            if (state != null && !state.isBlank()) {
                single.setState(state);
                changed = true;

                // Logic for DAMAGED state
                if ("DAMAGED".equals(state)) {
                    single.setDamageReason(damageReason);

                    // Notify users
                    createDamageNotifications(single, damageReason);
                }
            }
            if (changed) {
                itemSingleRepository.saveAndFlush(single);
            }
        });
    }

    private void createDamageNotifications(ItemSingle itemSingle, String damageReason) {
        if (itemSingle == null)
            return;

        // 2. Notify upcoming users (next 3 days)
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, 3);
        Date threeDaysLater = cal.getTime();

        List<Booking> upcomingBookings = bookingRepository.findOverlappingBookingsForItemSingle(
                itemSingle,
                now, // pickup (conservative)
                now, // start
                threeDaysLater, // end
                threeDaysLater // laundry (conservative)
        );

        for (Booking booking : upcomingBookings) {
            String msg = "A sua reserva para " + itemSingle.getItem().getName()
                    + " poderá ser afetada devido a danos no item."
                    + (damageReason != null ? " Motivo: " + damageReason : "");
            Notification notification = new Notification(booking.getUser(), msg);
            notificationRepository.save(notification);
        }
    }

    public int addItem(ItemDTO itemDTO, String size) {

        // Verificar se os atributos estão certos
        List<String> sizes = Arrays.asList("XS", "S", "M", "L", "XL");
        List<String> materials = Arrays.asList("Algodão", "Poliéster", "Seda", "Couro", "Veludo");

        if (!sizes.contains(size)) {
            return -1;
        }

        if (!materials.contains(itemDTO.getMaterial())) {
            return -2;
        }

        Optional<Item> found = itemRepository.findByAllCharacteristics(
                itemDTO.getName(), itemDTO.getMaterial(), itemDTO.getColor(), itemDTO.getBrand(),
                itemDTO.getGender(), itemDTO.getCategory(), itemDTO.getSubcategory(), itemDTO.getShopId());

        Item itemToUse;
        if (found.isEmpty()) {
            Item created = this.createItem(itemDTO);

            if (created == null) {
                // Shop inexistente ou itemType não encontrado
                return -3;
            }
            if (itemDTO.getImagePath() != null && !itemDTO.getImagePath().isEmpty()) {
                created.setImagePath(itemDTO.getImagePath());
            }
            itemToUse = itemRepository.saveAndFlush(created);
        } else {
            itemToUse = found.get();
        }

        // Criar unidade individual para o item (stock)
        ItemSingle itemSingle = new ItemSingle("AVAILABLE", itemToUse, size);

        itemSingleRepository.saveAndFlush(itemSingle);

        if (itemToUse.getItemId() != null) {
            itemDTO.setItemId(itemToUse.getItemId());
        }

        return 0;
    }

    public int updateItem(ItemDTO itemDTO) {
        Optional<Item> optionalItem = itemRepository.findById(itemDTO.getItemId());
        
        if (optionalItem.isEmpty()) {
            return -1;
        }

        Item itemToUpdate = optionalItem.get();
        boolean hasChanges = false;
        
        // Update basic fields
        hasChanges = updateBasicField(itemDTO.getName(), itemToUpdate::getName, itemToUpdate::setName) || hasChanges;
        hasChanges = updateBasicField(itemDTO.getBrand(), itemToUpdate::getBrand, itemToUpdate::setBrand) || hasChanges;
        hasChanges = updateBasicField(itemDTO.getMaterial(), itemToUpdate::getMaterial, itemToUpdate::setMaterial) || hasChanges;
        hasChanges = updateBasicField(itemDTO.getColor(), itemToUpdate::getColor, itemToUpdate::setColor) || hasChanges;
        
        // Update price fields
        hasChanges = updatePriceField(itemDTO.getPriceRent(), itemToUpdate::getPriceRent, itemToUpdate::setPriceRent) || hasChanges;
        hasChanges = updatePriceField(itemDTO.getPriceSale(), itemToUpdate::getPriceSale, itemToUpdate::setPriceSale) || hasChanges;
        
        // Update item type if needed
        hasChanges = updateItemType(itemDTO, itemToUpdate) || hasChanges;
        
        // Save if there were changes
        if (hasChanges) {
            itemRepository.save(itemToUpdate);
        }
        
        return 0;
    }
    
    private boolean updateBasicField(String newValue, java.util.function.Supplier<String> getter, java.util.function.Consumer<String> setter) {
        if (newValue == null || newValue.isBlank() || newValue.equals(getter.get())) {
            return false;
        }
        setter.accept(newValue);
        return true;
    }
    
    private boolean updatePriceField(BigDecimal newValue, java.util.function.Supplier<BigDecimal> getter, java.util.function.Consumer<BigDecimal> setter) {
        if (newValue == null || newValue.equals(getter.get())) {
            return false;
        }
        setter.accept(newValue);
        return true;
    }
    
    private boolean updateItemType(ItemDTO itemDTO, Item item) {
        if (!hasItemTypeUpdate(itemDTO)) {
            return false;
        }
        
        ItemType currentType = item.getItemType();
        String gender = getOrDefault(itemDTO.getGender(), currentType::getGender);
        String category = getOrDefault(itemDTO.getCategory(), currentType::getCategory);
        String subcategory = getOrDefault(itemDTO.getSubcategory(), currentType::getSubcategory);
        
        if (isItemTypeChanged(gender, category, subcategory, currentType)) {
            ItemType newItemType = itemTypeRepository.findByGenderAndCategoryAndSubcategory(
                gender, category, subcategory);
            
            if (newItemType != null) {
                item.setItemType(newItemType);
                return true;
            }
        }
        
        return false;
    }
    
    private boolean hasItemTypeUpdate(ItemDTO itemDTO) {
        return itemDTO.getGender() != null || itemDTO.getCategory() != null || itemDTO.getSubcategory() != null;
    }
    
    private String getOrDefault(String value, java.util.function.Supplier<String> defaultValueSupplier) {
        return value != null ? value : defaultValueSupplier.get();
    }
    
    private boolean isItemTypeChanged(String gender, String category, String subcategory, ItemType currentType) {
        return !gender.equals(currentType.getGender()) ||
               !category.equals(currentType.getCategory()) ||
               !subcategory.equals(currentType.getSubcategory());
    }

    public Staff login(String usernameOrEmail, String password) {
        // Tentar encontrar por email primeiro
        Optional<Staff> staffByEmail = staffRepository.findByEmail(usernameOrEmail);
        if (staffByEmail.isPresent()) {
            Staff staff = staffByEmail.get();
            if (passwordEncoder.matches(password, staff.getPassword())) {
                logger.info("Staff login successful by email");
                return staff;
            }
        }

        // Se não encontrou por email, tentar por username
        Optional<Staff> staffByUsername = staffRepository.findByUsername(usernameOrEmail);
        if (staffByUsername.isPresent()) {
            Staff staff = staffByUsername.get();
            if (passwordEncoder.matches(password, staff.getPassword())) {
                logger.info("Staff login successful by username");
                return staff;
            }
        }

        logger.warn("Failed staff login attempt");
        return null;
    }

    // Auxiliar Methods
    private Item createItem(ItemDTO itemDTO) {
        Optional<Shop> optionalShop = shopRepository.findById(itemDTO.getShopId());
        ItemType itemType = itemTypeRepository.findByGenderAndCategoryAndSubcategory(itemDTO.getGender(),
                itemDTO.getCategory(), itemDTO.getSubcategory());

        if (optionalShop.isEmpty() || itemType == null)
            return null;

        Shop shop = optionalShop.get();

        return new Item(itemDTO.getName(), itemDTO.getMaterial(), itemDTO.getColor(), itemDTO.getBrand(),
                itemDTO.getPriceRent(), itemDTO.getPriceSale(), shop, itemType);
    }

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }
}