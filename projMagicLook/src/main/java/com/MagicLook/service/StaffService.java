package com.magiclook.service;

import com.MagicLook.data.*;
import com.MagicLook.dto.*;
import com.MagicLook.repository.*;
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

    StaffService (ItemRepository itemRepository, ShopRepository shopRepository, ItemTypeRepository itemTypeRepository, ItemSingleRepository itemSingleRepository) {
        this.itemRepository = itemRepository;
        this.shopRepository = shopRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.itemSingleRepository = itemSingleRepository;
    }

    public String saveImage(MultipartFile image, Integer itemId) throws IOException {
        System.out.println("=== SAVE IMAGE ===");
        System.out.println("Image null? " + (image == null));
        
        if (image == null || image.isEmpty()) {
            System.out.println("Imagem vazia ou null, retornando null");
            return null;
        }

        System.out.println("Nome original: " + image.getOriginalFilename());
        System.out.println("Tamanho: " + image.getSize() + " bytes");
        System.out.println("Content-Type: " + image.getContentType());
        System.out.println("Upload dir: " + uploadDir);

        Path uploadPath = Paths.get(uploadDir);
        System.out.println("Upload path: " + uploadPath.toAbsolutePath());
        
        Files.createDirectories(uploadPath);
        System.out.println("Diretório criado/verificado");

        String safeOriginal = image.getOriginalFilename() == null ? "file" : image.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
        String idPart = (itemId != null) ? String.valueOf(itemId) : UUID.randomUUID().toString().substring(0,8);
        String fileName = String.format("item_%s_%s", idPart, safeOriginal);
        System.out.println("Nome do ficheiro: " + fileName);
        
        Path filePath = uploadPath.resolve(fileName);
        System.out.println("Caminho completo: " + filePath.toAbsolutePath());

        image.transferTo(filePath);
        System.out.println("Ficheiro guardado com sucesso!");

        String returnPath = uploadDir + "/" + fileName;
        System.out.println("Caminho retornado: " + returnPath);
        
        return returnPath;
    }

    public int addItem(ItemDTO itemDTO) {

        // Verificar se os atributos estão certos
        List<String> sizes = Arrays.asList("XS", "S", "M", "L", "XL");
        List<String> materials = Arrays.asList("Algodão", "Poliéster", "Seda", "Couro","Veludo");

        if (!sizes.contains(itemDTO.getSize())) {
            return -1;
        }

        if (!materials.contains(itemDTO.getMaterial())) {
            return -2;
        }

        List<Item> found = itemRepository.findByNameAndMaterialAndColorAndBrandAndSize(
            itemDTO.getName(), itemDTO.getMaterial(), itemDTO.getColor(), itemDTO.getBrand(), itemDTO.getSize());

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
            itemToUse = found.get(0);
        }

        // Criar unidade individual para o item (stock)
        ItemSingle itemSingle = new ItemSingle("AVAILABLE", itemToUse);

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
                new Staff("Ana Silva", "ana.silva@magiclook.com", "admin123", "anasi", shops.get(0)),
                new Staff("Carlos Santos", "carlos.santos@magiclook.com", "admin456", "carloss", shops.get(1)),
                new Staff("Maria Oliveira", "maria.oliveira@magiclook.com", "admin789", "mariao", shops.get(2))
            );
            
            staffRepository.saveAll(staffList);
            System.out.println("Staff inicializado com 3 administradores");
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

        return new Item(itemDTO.getName(), itemDTO.getMaterial(), itemDTO.getColor(), itemDTO.getBrand(), itemDTO.getSize(), itemDTO.getPriceRent(), itemDTO.getPriceSale(), shop, itemType);
    }
    
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }
}