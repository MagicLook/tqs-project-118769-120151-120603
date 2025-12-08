package com.MagicLook.services;

import com.MagicLook.data.Item;
import com.MagicLook.data.Shop;
import com.MagicLook.service.ItemService;
import com.MagicLook.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void testGetAllItems_ReturnsList() {
        Item item1 = new Item();
        Item item2 = new Item();
        List<Item> items = Arrays.asList(item1, item2);

        when(itemRepository.findAll()).thenReturn(items);

        List<Item> result = itemService.getAllItems();

        assertEquals(2, result.size());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void testGetAllItems_ReturnsEmptyList() {
        when(itemRepository.findAll()).thenReturn(Collections.emptyList());

        List<Item> result = itemService.getAllItems();

        assertTrue(result.isEmpty());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void testGetItemsByShop_ReturnsList() {
        Shop shop = new Shop();
        Item item1 = new Item();
        Item item2 = new Item();
        List<Item> items = Arrays.asList(item1, item2);

        when(itemRepository.findByShop(shop)).thenReturn(items);

        List<Item> result = itemService.getItemsByShop(shop);

        assertEquals(2, result.size());
        verify(itemRepository, times(1)).findByShop(shop);
    }

    @Test
    void testGetItemsByShop_ReturnsEmptyList() {
        Shop shop = new Shop();

        when(itemRepository.findByShop(shop)).thenReturn(Collections.emptyList());

        List<Item> result = itemService.getItemsByShop(shop);

        assertTrue(result.isEmpty());
        verify(itemRepository, times(1)).findByShop(shop);
    }
}