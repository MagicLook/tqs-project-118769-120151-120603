package com.MagicLook.mainpage;

import com.MagicLook.data.Item;
import com.MagicLook.data.ItemType;
import com.MagicLook.data.Shop;
import com.MagicLook.repository.ItemRepository;
import com.MagicLook.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    
    @Mock
    private ItemRepository itemRepository;
    
    private ItemService itemService;
    
    @BeforeEach
    public void setUp() {
        // Agora precisamos passar o repositório no construtor
        itemService = new ItemService(itemRepository);
    }
    
    @Test
    public void testGetItemsByGender_Male() {
        // Arrange
        Item item1 = createItem("Camisa", "M");
        Item item2 = createItem("Calça", "M");
        List<Item> maleItems = Arrays.asList(item1, item2);
        
        when(itemRepository.findByItemTypeGender("M")).thenReturn(maleItems);
        
        // Act
        List<Item> result = itemService.getItemsByGender("M");
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Camisa", result.get(0).getName());
        assertEquals("Calça", result.get(1).getName());
    }
    
    @Test
    public void testGetItemsByGender_Female() {
        // Arrange
        Item item1 = createItem("Vestido", "F");
        Item item2 = createItem("Saia", "F");
        List<Item> femaleItems = Arrays.asList(item1, item2);
        
        when(itemRepository.findByItemTypeGender("F")).thenReturn(femaleItems);
        
        // Act
        List<Item> result = itemService.getItemsByGender("F");
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Vestido", result.get(0).getName());
        assertEquals("Saia", result.get(1).getName());
    }
    
    @Test
    public void testGetRecentItems_Limit() {
        // Arrange
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
        
        // Act
        List<Item> result = itemService.getRecentItems(5);
        
        // Assert
        assertEquals(5, result.size());
        assertEquals("Item1", result.get(0).getName());
        assertEquals("Item5", result.get(4).getName());
    }
    
    @Test
    public void testGetRecentItems_LessThanLimit() {
        // Arrange
        List<Item> allItems = Arrays.asList(
            createItem("Item1", "M"),
            createItem("Item2", "F")
        );
        
        when(itemRepository.findAll()).thenReturn(allItems);
        
        // Act
        List<Item> result = itemService.getRecentItems(5);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Item1", result.get(0).getName());
        assertEquals("Item2", result.get(1).getName());
    }
    
    @Test
    public void testGetItemsByShop() {
        // Arrange
        Shop shop = new Shop();
        shop.setShopId(1);
        
        Item item1 = createItem("Item1", "M");
        Item item2 = createItem("Item2", "F");
        List<Item> shopItems = Arrays.asList(item1, item2);
        
        when(itemRepository.findByShop(shop)).thenReturn(shopItems);
        
        // Act
        List<Item> result = itemService.getItemsByShop(shop);
        
        // Assert
        assertEquals(2, result.size());
        verify(itemRepository).findByShop(shop);
    }
    
    @Test
    public void testGetAllItems() {
        // Arrange
        List<Item> allItems = Arrays.asList(
            createItem("Item1", "M"),
            createItem("Item2", "F"),
            createItem("Item3", "M")
        );
        
        when(itemRepository.findAll()).thenReturn(allItems);
        
        // Act
        List<Item> result = itemService.getAllItems();
        
        // Assert
        assertEquals(3, result.size());
        verify(itemRepository).findAll();
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