package com.MagicLook.service;

import com.MagicLook.data.Item;
import com.MagicLook.data.Shop;
import com.MagicLook.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemService {
    
    @Autowired
    private ItemRepository itemRepository;
    
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
}