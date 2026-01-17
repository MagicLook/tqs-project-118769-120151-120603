package com.magiclook.services;

import com.magiclook.data.Item;
import com.magiclook.data.ItemSingle;
import com.magiclook.data.Shop;
import com.magiclook.dto.ItemFilterDTO;
import com.magiclook.repository.ItemRepository;
import com.magiclook.repository.ItemSingleRepository;
import com.magiclook.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemSingleRepository itemSingleRepository;

    @InjectMocks
    private ItemService itemService;

    private Item item;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setItemId(1);
        item.setName("Test Item");
    }

    @Test
    void testGetItemById_NullId() {
        assertTrue(itemService.getItemById(null).isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void testGetItemById_Found() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(item));
        var res = itemService.getItemById(1);
        assertTrue(res.isPresent());
        assertEquals(item, res.get());
        verify(itemRepository, times(1)).findById(1);
    }

    @Test
    void testGetItemById_ExceptionHandled() {
        when(itemRepository.findById(1)).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> itemService.getItemById(1));
        verify(itemRepository, times(1)).findById(1);
    }

    @Test
    void testGetAvailableSizesForItem_DistinctAndSorted() {
        ItemSingle s1 = new ItemSingle("AVAILABLE", item, "M");
        ItemSingle s2 = new ItemSingle("AVAILABLE", item, "M");
        ItemSingle s3 = new ItemSingle("AVAILABLE", item, "L");
        ItemSingle s4 = new ItemSingle("MAINTENANCE", item, "S");

        when(itemSingleRepository.findByItem_ItemId(1)).thenReturn(List.of(s1, s2, s3, s4));

        List<String> sizes = itemService.getAvailableSizesForItem(1);

        assertNotNull(sizes);
        // Sorted lexicographically: L then M
        assertEquals(List.of("L", "M"), sizes);
        verify(itemSingleRepository, times(1)).findByItem_ItemId(1);
    }

    @Test
    void testGetAllDistinctSubcategoriesByGender_Delegates() {
        when(itemRepository.findAllDistinctSubcategoriesByGender("M")).thenReturn(List.of("Casual", "Formal"));
        List<String> res = itemService.getAllDistinctSubcategoriesByGender("M");
        assertEquals(2, res.size());
        verify(itemRepository, times(1)).findAllDistinctSubcategoriesByGender("M");
    }

    @Test
    void testGetAllDistinctSizesByGender_Delegates() {
        when(itemRepository.findAllDistinctSizesByGender("F")).thenReturn(List.of("S", "M", "L"));
        List<String> res = itemService.getAllDistinctSizesByGender("F");
        assertEquals(3, res.size());
        verify(itemRepository, times(1)).findAllDistinctSizesByGender("F");
    }

    @Test
    void testFindByGenderAndFilters_PassesFilterFields() {
        ItemFilterDTO filter = new ItemFilterDTO();
        filter.setColor("Red");
        filter.setBrand("Acme");
        filter.setMaterial("Cotton");
        filter.setCategory("Shirt");
        filter.setSubcategory("Casual");
        filter.setSize("M");
        filter.setShopLocation("Lisboa");
        filter.setMinPrice(10.0);
        filter.setMaxPrice(100.0);

        when(itemRepository.findByGenderAndFilters(eq("M"), eq("Red"), eq("Acme"), eq("Cotton"), eq("Shirt"), eq("Casual"), eq("M"), eq("Lisboa"), eq(10.0), eq(100.0)))
            .thenReturn(List.of(item));

        List<Item> res = itemService.findByGenderAndFilters("M", filter);
        assertNotNull(res);
        assertEquals(1, res.size());
        verify(itemRepository, times(1)).findByGenderAndFilters(eq("M"), eq("Red"), eq("Acme"), eq("Cotton"), eq("Shirt"), eq("Casual"), eq("M"), eq("Lisboa"), eq(10.0), eq(100.0));
    }

    @Test
    void testSearchItemsWithFilters_CleansEmptyStrings() {
        when(itemRepository.findByGenderAndFilters(eq("F"), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0.0), eq(50.0)))
                .thenReturn(List.of(item));

            List<Item> res = itemService.searchItemsWithFilters("F", "", "", "", "", "", 0.0, 50.0);
            assertEquals(1, res.size());
            verify(itemRepository, times(1)).findByGenderAndFilters(eq("F"), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0.0), eq(50.0));
    }

    @Test
    void testGetItems_Delegates() {
        ItemSingle single = new ItemSingle("AVAILABLE", item, "M");
        when(itemSingleRepository.findByItem_ItemId(1)).thenReturn(List.of(single));
        List res = itemService.getItems(1);
        assertEquals(1, res.size());
        verify(itemSingleRepository, times(1)).findByItem_ItemId(1);
    }

    @Test
    void testGetAllItemsByState_Delegates() {
        when(itemRepository.findByItemSinglesState("AVAILABLE")).thenReturn(List.of(item));
        List<Item> res = itemService.getAllItemsByState("AVAILABLE");
        assertEquals(1, res.size());
        verify(itemRepository, times(1)).findByItemSinglesState("AVAILABLE");
    }

    @Test
    void testSave_DelegatesToRepository() {
        when(itemRepository.save(item)).thenReturn(item);
        Item saved = itemService.save(item);
        assertEquals(item, saved);
        verify(itemRepository, times(1)).save(item);
    }
}