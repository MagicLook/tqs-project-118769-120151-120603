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
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemSingleRepository itemSingleRepository;

    @InjectMocks
    private ItemService itemService;

    private Item item;
    private Shop shop;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setItemId(1);
        item.setName("Test Item");
        
        shop = new Shop();
        shop.setShopId(1);
        shop.setLocation("Lisboa");
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
    void testGetItemById_NotFound() {
        when(itemRepository.findById(999)).thenReturn(Optional.empty());
        var res = itemService.getItemById(999);
        assertTrue(res.isEmpty());
        verify(itemRepository, times(1)).findById(999);
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
    void testGetAvailableSizesForItem_NullId() {
        List<String> sizes = itemService.getAvailableSizesForItem(null);
        assertNotNull(sizes);
        assertTrue(sizes.isEmpty());
        verifyNoInteractions(itemSingleRepository);
    }

    @Test
    void testGetAvailableSizesForItem_NoAvailableSizes() {
        ItemSingle s1 = new ItemSingle("SOLD", item, "M");
        ItemSingle s2 = new ItemSingle("MAINTENANCE", item, "L");

        when(itemSingleRepository.findByItem_ItemId(1)).thenReturn(List.of(s1, s2));

        List<String> sizes = itemService.getAvailableSizesForItem(1);
        assertNotNull(sizes);
        assertTrue(sizes.isEmpty());
    }

    @Test
    void testGetAllDistinctSubcategoriesByGender_Delegates() {
        when(itemRepository.findAllDistinctSubcategoriesByGender("M")).thenReturn(List.of("Casual", "Formal"));
        List<String> res = itemService.getAllDistinctSubcategoriesByGender("M");
        assertEquals(2, res.size());
        verify(itemRepository, times(1)).findAllDistinctSubcategoriesByGender("M");
    }

    @Test
    void testGetAllDistinctSubcategoriesByGender_NullGender() {
        List<String> res = itemService.getAllDistinctSubcategoriesByGender(null);
        assertNotNull(res);
        assertTrue(res.isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void testGetAllDistinctSubcategoriesByGender_EmptyGender() {
        List<String> res = itemService.getAllDistinctSubcategoriesByGender("");
        assertNotNull(res);
        assertTrue(res.isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void testGetAllDistinctSizesByGender_Delegates() {
        when(itemRepository.findAllDistinctSizesByGender("F")).thenReturn(List.of("S", "M", "L"));
        List<String> res = itemService.getAllDistinctSizesByGender("F");
        assertEquals(3, res.size());
        verify(itemRepository, times(1)).findAllDistinctSizesByGender("F");
    }

    @Test
    void testGetAllDistinctSizesByGender_NullGender() {
        List<String> res = itemService.getAllDistinctSizesByGender(null);
        assertNotNull(res);
        assertTrue(res.isEmpty());
        verifyNoInteractions(itemRepository);
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

        when(itemRepository.findByGenderAndFilters("M", "Red", "Acme", "Cotton", "Shirt", "Casual", "M", "Lisboa", 10.0, 100.0))
            .thenReturn(List.of(item));

        List<Item> res = itemService.findByGenderAndFilters("M", filter);
        assertNotNull(res);
        assertEquals(1, res.size());
        verify(itemRepository, times(1)).findByGenderAndFilters("M", "Red", "Acme", "Cotton", "Shirt", "Casual", "M", "Lisboa", 10.0, 100.0);
    }

    @Test
    void testFindByGenderAndFilters_NullGender() {
        ItemFilterDTO filter = new ItemFilterDTO();
        List<Item> res = itemService.findByGenderAndFilters(null, filter);
        assertNotNull(res);
        assertTrue(res.isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void testFindByGenderAndFilters_NullFilter() {
        List<Item> res = itemService.findByGenderAndFilters("M", null);
        assertNotNull(res);
        assertTrue(res.isEmpty());
        verifyNoInteractions(itemRepository);
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
    void testSearchItemsWithFilters_NullValues() {
        when(itemRepository.findByGenderAndFilters(eq("M"), isNull(), isNull(), isNull(), isNull(), isNull(), eq(10.0), eq(100.0)))
                .thenReturn(List.of(item));

        List<Item> res = itemService.searchItemsWithFilters("M", null, null, null, null, null, 10.0, 100.0);
        assertEquals(1, res.size());
        verify(itemRepository, times(1)).findByGenderAndFilters(eq("M"), isNull(), isNull(), isNull(), isNull(), isNull(), eq(10.0), eq(100.0));
    }

    @Test
    void testGetItems_Delegates() {
        ItemSingle single = new ItemSingle("AVAILABLE", item, "M");
        when(itemSingleRepository.findByItem_ItemId(1)).thenReturn(List.of(single));
        List<ItemSingle> res = itemService.getItems(1);
        assertEquals(1, res.size());
        verify(itemSingleRepository, times(1)).findByItem_ItemId(1);
    }

    @Test
    void testGetItems_NullId() {
        List<ItemSingle> res = itemService.getItems(null);
        assertNotNull(res);
        assertTrue(res.isEmpty());
        verifyNoInteractions(itemSingleRepository);
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

    @Test
    void testGetItemsByShop_NullShop() {
        List<Item> res = itemService.getItemsByShop(null);
        assertNotNull(res);
        assertTrue(res.isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void testGetItemsByShop_ValidShop() {
        when(itemRepository.findByShop(shop)).thenReturn(List.of(item));
        List<Item> res = itemService.getItemsByShop(shop);
        assertEquals(1, res.size());
        verify(itemRepository, times(1)).findByShop(shop);
    }

    @Test
    void testGetAllItems() {
        when(itemRepository.findAll()).thenReturn(List.of(item, new Item()));
        List<Item> res = itemService.getAllItems();
        assertEquals(2, res.size());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void testGetItemsByGender_NullGender() {
        List<Item> res = itemService.getItemsByGender(null);
        assertNotNull(res);
        assertTrue(res.isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void testGetItemsByGender_EmptyGender() {
        List<Item> res = itemService.getItemsByGender("");
        assertNotNull(res);
        assertTrue(res.isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void testGetItemsByGender_ValidGender() {
        when(itemRepository.findByItemTypeGender("M")).thenReturn(List.of(item));
        List<Item> res = itemService.getItemsByGender("M");
        assertEquals(1, res.size());
        verify(itemRepository, times(1)).findByItemTypeGender("M");
    }

    @Test
    void testGetRecentItems() {
        Item item2 = new Item();
        item2.setItemId(2);
        
        when(itemRepository.findAll()).thenReturn(List.of(item, item2));
        
        // Test with limit 1
        List<Item> res = itemService.getRecentItems(1);
        assertEquals(1, res.size());
        assertEquals(1, res.get(0).getItemId());
        
        // Test with limit greater than list size
        res = itemService.getRecentItems(5);
        assertEquals(2, res.size());
        
        verify(itemRepository, times(2)).findAll();
    }

    @Test
    void testGetRecentItems_ZeroLimit() {
        List<Item> res = itemService.getRecentItems(0);
        assertNotNull(res);
        assertTrue(res.isEmpty());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void testGetAllDistinctColors() {
        when(itemRepository.findAllDistinctColors()).thenReturn(List.of("Red", "Blue", "Green"));
        List<String> res = itemService.getAllDistinctColors();
        assertEquals(3, res.size());
        verify(itemRepository, times(1)).findAllDistinctColors();
    }

    @Test
    void testGetAllDistinctBrands() {
        when(itemRepository.findAllDistinctBrands()).thenReturn(List.of("Nike", "Adidas", "Puma"));
        List<String> res = itemService.getAllDistinctBrands();
        assertEquals(3, res.size());
        verify(itemRepository, times(1)).findAllDistinctBrands();
    }

    @Test
    void testGetAllDistinctMaterials() {
        when(itemRepository.findAllDistinctMaterials()).thenReturn(List.of("Cotton", "Polyester", "Wool"));
        List<String> res = itemService.getAllDistinctMaterials();
        assertEquals(3, res.size());
        verify(itemRepository, times(1)).findAllDistinctMaterials();
    }

    @Test
    void testGetAllDistinctCategories() {
        when(itemRepository.findAllDistinctCategories()).thenReturn(List.of("Shirts", "Pants", "Shoes"));
        List<String> res = itemService.getAllDistinctCategories();
        assertEquals(3, res.size());
        verify(itemRepository, times(1)).findAllDistinctCategories();
    }

    @Test
    void testGetAllDistinctShopLocations() {
        when(itemRepository.findAllDistinctShopLocations()).thenReturn(List.of("Lisboa", "Porto", "Faro"));
        List<String> res = itemService.getAllDistinctShopLocations();
        assertEquals(3, res.size());
        verify(itemRepository, times(1)).findAllDistinctShopLocations();
    }
}