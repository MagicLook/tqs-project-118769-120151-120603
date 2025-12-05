package com.MagicLook.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.MagicLook.dto.ItemDTO;
import com.MagicLook.repository.*;
import com.MagicLook.data.*;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class StaffService extends ClientService {
    private ItemRepository itemRepository;
    private ShopRepository shopRepository;
    private ItemTypeRepository itemTypeRepository;

    // Constructor
    StaffService (ItemRepository itemRepository, ShopRepository shopRepository, ItemTypeRepository itemTypeRepository) {
        this.itemRepository = itemRepository;
        this.shopRepository = shopRepository;
        this.itemTypeRepository = itemTypeRepository;
    }

    public int addItem(ItemDTO itemDTO) {
        // Check if exists
        List<Item> result = itemRepository.findByNameAndMaterialAndColorAndBrandAndSize(itemDTO.getName(), itemDTO.getMaterial(), itemDTO.getColor(), itemDTO.getBrand(), itemDTO.getSize());

        if (!result.isEmpty()) {
            return -1;
        }

        // If not, adds, but before must occur a conversion.
        Item item = this.createItem(itemDTO);

        if (item == null) {
            return -2;
        } 

        itemRepository.saveAndFlush(item);
        
        return 0;
    }

    // Auxiliar Methods
    private Item createItem(ItemDTO itemDTO) {
        Optional<Shop> optionalShop = shopRepository.findById(itemDTO.getShopId());
        Optional<ItemType> optionalItemType = itemTypeRepository.findById(itemDTO.getItemTypeId());

        if (optionalShop.isEmpty() && optionalItemType.isEmpty())
            return null;

        Shop shop = optionalShop.get();
        ItemType itemType = optionalItemType.get();

        return new Item(itemDTO.getName(), itemDTO.getMaterial(), itemDTO.getColor(), itemDTO.getBrand(), itemDTO.getSize(), itemDTO.getPriceRent(), itemDTO.getPriceSale(), shop, itemType);
    }
}
