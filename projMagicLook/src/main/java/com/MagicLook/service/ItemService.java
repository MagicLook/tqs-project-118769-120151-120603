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
}