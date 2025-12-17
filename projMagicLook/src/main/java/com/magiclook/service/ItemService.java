package com.magiclook.service;

import com.magiclook.data.Item;
import com.magiclook.data.ItemSingle;
import com.magiclook.data.Shop;
import com.magiclook.repository.ItemRepository;
import com.magiclook.repository.ItemSingleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

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
        return itemRepository.findById(itemId).orElse(null);
    }
}