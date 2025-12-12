package com.MagicLook.service;

import com.MagicLook.data.*;
import com.MagicLook.dto.*;
import com.MagicLook.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
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

    private ItemSingleRepository itemSingleRepository;

    StaffService (ItemRepository itemRepository, ShopRepository shopRepository, ItemTypeRepository itemTypeRepository) {
        this.itemRepository = itemRepository;
        this.shopRepository = shopRepository;
        this.itemTypeRepository = itemTypeRepository;
    }

    public int addItem(ItemDTO itemDTO) {
        // Verificar se os atributos estão certos

        List<String> sizes = Arrays.asList("XS", "S", "M", "L", "XL");
        List<String> materials = Arrays.asList("Algodão", "Poliéster", "Lã", "Seda", "Couro", "Nylon", "Linho", "Veludo", "Jeans");


        if (!sizes.contains(itemDTO.getSize())) {
            return -1;
        }

        if (!materials.contains(itemDTO.getMaterial())) {
            return -2;
        }

        List<Item> result = itemRepository.findByNameAndMaterialAndColorAndBrandAndSize(itemDTO.getName(), itemDTO.getMaterial(), itemDTO.getColor(), itemDTO.getBrand(), itemDTO.getSize());

        if (result.isEmpty()) {

            Item item = this.createItem(itemDTO);
        
            itemRepository.saveAndFlush(item);

        }
        
        ItemSingle itemSingle = new ItemSingle("AVAILABLE", this.createItem(itemDTO));

        itemSingleRepository.saveAndFlush(itemSingle);
        
        return 0;
    }

    @PostConstruct
    public void initializeStaff() {
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
        Optional<ItemType> optionalItemType = itemTypeRepository.findById(itemDTO.getItemTypeId());

        if (optionalShop.isEmpty() || optionalItemType.isEmpty())
            return null;

        Shop shop = optionalShop.get();
        ItemType itemType = optionalItemType.get();

        return new Item(itemDTO.getName(), itemDTO.getMaterial(), itemDTO.getColor(), itemDTO.getBrand(), itemDTO.getSize(), itemDTO.getPriceRent(), itemDTO.getPriceSale(), shop, itemType);
    }
    
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }
}