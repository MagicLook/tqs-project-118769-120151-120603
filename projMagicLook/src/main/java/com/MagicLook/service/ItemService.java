package com.MagicLook.service;

import com.MagicLook.data.Item;
import com.MagicLook.data.Shop;
import com.MagicLook.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemService {
    
    private final ItemRepository itemRepository;
    
    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
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
        List<Item> allItems = itemRepository.findAll();
        if (allItems.size() > limit) {
            return allItems.subList(0, limit);
        }
        return allItems;
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

    public List<Item> searchItemsWithFilters(String gender, String color, String brand, 
                                             String material, String category, 
                                             Double minPrice, Double maxPrice) {
        return itemRepository.findByGenderAndFilters(gender, color, brand, material, 
                                                    category, minPrice, maxPrice);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }
}