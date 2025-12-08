package com.MagicLook.mainpage;

import com.MagicLook.data.Item;
import com.MagicLook.data.ItemType;
import com.MagicLook.data.Shop;
import com.MagicLook.repository.ItemRepository;
import com.MagicLook.service.ItemService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemServiceTest {
    
    @Mock private ItemRepository itemRepository;
    
    @InjectMocks private ItemService itemService;
    
    public ItemServiceTest() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    public void testGetItemsByGender_Male() {
        Item item1 = createItem("Camisa", "M");
        Item item2 = createItem("Calça", "M");
        Item item3 = createItem("Vestido", "F");
        
        List<Item> allItems = Arrays.asList(item1, item2, item3);
        List<Item> maleItems = Arrays.asList(item1, item2);
        
        when(itemRepository.findByItemTypeGender("M")).thenReturn(maleItems);
        
        List<Item> result = itemService.getItemsByGender("M");
        
        assertEquals(2, result.size());
        assertEquals("Camisa", result.get(0).getName());
        assertEquals("Calça", result.get(1).getName());
    }
    
    @Test
    public void testGetItemsByGender_Female() {
        Item item1 = createItem("Camisa", "M");
        Item item2 = createItem("Vestido", "F");
        Item item3 = createItem("Saia", "F");
        
        List<Item> femaleItems = Arrays.asList(item2, item3);
        
        when(itemRepository.findByItemTypeGender("F")).thenReturn(femaleItems);
        List<Item> result = itemService.getItemsByGender("F");
        assertEquals(2, result.size());
        assertEquals("Vestido", result.get(0).getName());
        assertEquals("Saia", result.get(1).getName());
    }
    
    @Test
    public void testGetRecentItems_Limit() {
        List<Item> allItems = Arrays.asList(
            createItem("Item1", "M"),
            createItem("Item2", "F"),
            createItem("Item3", "M"),
            createItem("Item4", "F"),
            createItem("Item5", "M"),
            createItem("Item6", "F"),
            createItem("Item7", "M")
        );
        
        when(itemRepository.findAll()).thenReturn(allItems);
        List<Item> result = itemService.getRecentItems(5);
        assertEquals(5, result.size());
    }
    
    @Test
    public void testGetRecentItems_LessThanLimit() {
        List<Item> allItems = Arrays.asList(
            createItem("Item1", "M"),
            createItem("Item2", "F")
        );
        
        when(itemRepository.findAll()).thenReturn(allItems);
        List<Item> result = itemService.getRecentItems(5);
        assertEquals(2, result.size());
    }
    
    private Item createItem(String name, String gender) {
        Item item = new Item();
        item.setItemId(UUID.randomUUID());
        item.setName(name);
        item.setPriceRent(new BigDecimal("10.00"));
        item.setPriceSale(new BigDecimal("50.00"));
        
        ItemType itemType = new ItemType();
        itemType.setGender(gender);
        item.setItemType(itemType);
        
        Shop shop = new Shop();
        shop.setShopId(1);
        item.setShop(shop);
        
        return item;
    }
}