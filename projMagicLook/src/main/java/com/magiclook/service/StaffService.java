package com.magiclook.service;

import com.magiclook.data.*;
import com.magiclook.dto.*;
import com.magiclook.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.*;
import java.util.Optional;
import java.math.BigDecimal;
import java.io.File;


@Service
@Transactional
public class StaffService extends ClientService {
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;
    
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemSingleRepository itemSingleRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Autowired
    StaffService (StaffRepository staffRepository, ItemRepository itemRepository, ShopRepository shopRepository, ItemTypeRepository itemTypeRepository, ItemSingleRepository itemSingleRepository) {
        this.staffRepository = staffRepository;
        this.itemRepository = itemRepository;
        this.shopRepository = shopRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.itemSingleRepository = itemSingleRepository;
    }

    public String saveImage(MultipartFile image, Integer itemId) throws IOException {

        if (image == null || image.isEmpty()) {
            return null;
        }

        // Normalize target directory to live under static so Spring can serve it
        String normalizedDir = uploadDir.startsWith("/") ? uploadDir.substring(1) : uploadDir;
        Path staticBase = Paths.get("src/main/resources/static").toAbsolutePath();
        Path uploadPath = staticBase.resolve(normalizedDir);

        Files.createDirectories(uploadPath);

        String safeOriginal = image.getOriginalFilename() == null ? "file" : image.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
        String idPart = (itemId != null) ? String.valueOf(itemId) : UUID.randomUUID().toString().substring(0, 8);
        String fileName = String.format("item_%s_%s", idPart, safeOriginal);

        Path filePath = uploadPath.resolve(fileName);

        image.transferTo(filePath.toFile());

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

    public int addItem(ItemDTO itemDTO, String size) {

        // Verificar se os atributos estão certos
        List<String> sizes = Arrays.asList("XS", "S", "M", "L", "XL");
        List<String> materials = Arrays.asList("Algodão", "Poliéster", "Seda", "Couro","Veludo");

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

    @PostConstruct
    public void initializeStaff() {
        seedDefaultItemTypes();

        if (staffRepository.count() == 0) {
            // Criar lojas se não existirem
            List<Shop> shops = Arrays.asList(
                new Shop("Loja Centro", "Centro Comercial ABC"),
                new Shop("Loja Norte", "Shopping Norte"),
                new Shop("Loja Sul", "Avenida Sul, 123")
            );
            
            shops = shopRepository.saveAll(shops);
            
            List<Staff> staffList = Arrays.asList(
                new Staff("Ana Silva", "ana.silva@magiclook.com", "admin123", "admin", shops.get(0)),
                new Staff("Carlos Santos", "carlos.santos@magiclook.com", "admin456", "carloss", shops.get(1)),
                new Staff("Maria Oliveira", "maria.oliveira@magiclook.com", "admin789", "mariao", shops.get(2))
            );
            
            seedDefaultItems();

            staffRepository.saveAll(staffList);
        }
    }

    private void seedDefaultItemTypes() {
        // Estrutura: gender -> categoria -> lista de subcategorias
        Map<String, Map<String, List<String>>> defaults = new HashMap<>();
        
        // MULHER (usar nomes no singular para corresponder ao frontend)
        Map<String, List<String>> female = new HashMap<>();
        female.put("Vestido", Arrays.asList("Curto", "Médio", "Longo"));
        female.put("Macacão", Arrays.asList("Curto", "Médio", "Longo"));
        defaults.put("F", female);
        
        // HOMEM (usar nomes no singular para corresponder ao frontend)
        Map<String, List<String>> male = new HashMap<>();
        male.put("Fato", Arrays.asList("Simples", "Três peças"));
        defaults.put("M", male);

        int created = 0;
        for (Map.Entry<String, Map<String, List<String>>> genderEntry : defaults.entrySet()) {
            String gender = genderEntry.getKey();
            for (Map.Entry<String, List<String>> categoryEntry : genderEntry.getValue().entrySet()) {
                String category = categoryEntry.getKey();
                for (String subcategory : categoryEntry.getValue()) {
                    ItemType existing = itemTypeRepository.findByGenderAndCategoryAndSubcategory(gender, category, subcategory);
                    if (existing == null) {
                        itemTypeRepository.save(new ItemType(gender, category, subcategory));
                        created++;
                    }
                }
            }
        }
        if (created > 0) {
            System.out.println("Seeded " + created + " ItemType(s)");
        }
    }

    private void seedDefaultItems() {
        if (itemRepository.count() == 0) {
            Item item = new Item(
                "Vestido Azul",
                "Seda",
                "Azul",
                "C&A",
                new BigDecimal("300.00"),
                new BigDecimal("6000.00"),
                shopRepository.findById(1).orElseThrow(),
                itemTypeRepository.findByGenderAndCategoryAndSubcategory("F", "Vestido", "Médio")
            );

            item.setImagePath(uploadDir +"/default.jpg");
            
            itemRepository.save(item);

            ItemSingle itemSingle = new ItemSingle("AVAILABLE", item, "M");

            itemSingleRepository.save(itemSingle);

            item = new Item(
                "Vestido Vermelho",
                "Seda",
                "Vermelho",
                "Zara",
                new BigDecimal("300.00"),
                new BigDecimal("6000.00"),
                shopRepository.findById(1).orElseThrow(),
                itemTypeRepository.findByGenderAndCategoryAndSubcategory("F", "Vestido", "Médio")
            );

            item.setImagePath(uploadDir +"/default.jpg");
            
            itemRepository.save(item);   

            itemSingle = new ItemSingle("AVAILABLE", item, "S");

            itemSingleRepository.save(itemSingle);
        }
    }
    
    public Staff login(String usernameOrEmail, String password) {
        // Tentar encontrar por email primeiro
        Optional<Staff> staffByEmail = staffRepository.findByEmail(usernameOrEmail);
        if (staffByEmail.isPresent()) {
            Staff staff = staffByEmail.get();
            if (staff.getPassword().equals(password)) {
                return staff;
            }
        }
        
        // Se não encontrou por email, tentar por username
        Optional<Staff> staffByUsername = staffRepository.findByUsername(usernameOrEmail);
        if (staffByUsername.isPresent()) {
            Staff staff = staffByUsername.get();
            if (staff.getPassword().equals(password)) {
                return staff;
            }
        }
        
        return null;
    }

    // Auxiliar Methods
    private Item createItem(ItemDTO itemDTO) {
        Optional<Shop> optionalShop = shopRepository.findById(itemDTO.getShopId());
        ItemType itemType = itemTypeRepository.findByGenderAndCategoryAndSubcategory(itemDTO.getGender(), itemDTO.getCategory(), itemDTO.getSubcategory());

        if (optionalShop.isEmpty() || itemType == null)
            return null;

        Shop shop = optionalShop.get();

        return new Item(itemDTO.getName(), itemDTO.getMaterial(), itemDTO.getColor(), itemDTO.getBrand(), itemDTO.getPriceRent(), itemDTO.getPriceSale(), shop, itemType);
    }
    
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }
}