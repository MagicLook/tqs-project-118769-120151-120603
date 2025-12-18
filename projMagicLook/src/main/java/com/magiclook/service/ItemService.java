package com.magiclook.service;

import com.magiclook.data.Item;
import com.magiclook.data.ItemSingle;
import com.magiclook.data.Shop;
import com.magiclook.repository.ItemRepository;
import com.magiclook.repository.ItemSingleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {
    
    private final ItemRepository itemRepository;
    private ItemSingleRepository itemSingleRepository;
    
    @Autowired
    public ItemService(ItemRepository itemRepository, ItemSingleRepository itemSingleRepository) {
        this.itemRepository = itemRepository;
        this.itemSingleRepository = itemSingleRepository;
    }

    public List<Item> getItemsByShop(Shop shop) {
        return itemRepository.findByShop(shop);
    }
    
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public List<Item> getItemsByGender(String gender) {
        return itemRepository.findByItemTypeGender(gender);
    }

    public List<Item> getRecentItems(int limit) {
        return itemRepository.findAll().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<String> getAllDistinctColors() {
        return itemRepository.findAllDistinctColors();
    }
    
    public List<String> getAllDistinctBrands() {
        return itemRepository.findAllDistinctBrands();
    }
    
    public List<String> getAllDistinctMaterials() {
        return itemRepository.findAllDistinctMaterials();
    }
    
    public List<String> getAllDistinctCategories() {
        return itemRepository.findAllDistinctCategories();
    }
    
    public List<String> getAllDistinctShopLocations() {
        return itemRepository.findAllDistinctShopLocations();
    }

    public List<Item> searchItemsWithFilters(String gender, String color, String brand, 
                                             String material, String category, 
                                             String shopLocation,
                                             Double minPrice, Double maxPrice) {
        // Converter strings vazias para null
        String cleanedColor = (color == null || color.isEmpty()) ? null : color;
        String cleanedBrand = (brand == null || brand.isEmpty()) ? null : brand;
        String cleanedMaterial = (material == null || material.isEmpty()) ? null : material;
        String cleanedCategory = (category == null || category.isEmpty()) ? null : category;
        String cleanedShopLocation = (shopLocation == null || shopLocation.isEmpty()) ? null : shopLocation;
        
        return itemRepository.findByGenderAndFilters(gender, cleanedColor, cleanedBrand, 
                                                    cleanedMaterial, cleanedCategory,
                                                    cleanedShopLocation, minPrice, maxPrice);
    }

    public List<Item> getAllItemsByState(String state) {
        return itemRepository.findByItemSinglesState(state);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public List<ItemSingle> getItems(Integer itemId) {
        return itemSingleRepository.findByItem_ItemId(itemId);
    }
    
    public Item getItemById(Integer itemId) {
        if (itemId == null) {
            return null;
        }
        try {
            return itemRepository.findById(itemId).orElse(null);
        } catch (Exception e) {
            System.err.println("Erro ao buscar item com ID " + itemId + ": " + e.getMessage());
            return null;
        }
    }
    
    // Método simplificado para obter tamanhos disponíveis
    public List<String> getAvailableSizesForItem(Integer itemId) {
        return itemSingleRepository.findByItem_ItemId(itemId)
            .stream()
            .filter(is -> "AVAILABLE".equals(is.getState()))
            .map(ItemSingle::getSize)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}