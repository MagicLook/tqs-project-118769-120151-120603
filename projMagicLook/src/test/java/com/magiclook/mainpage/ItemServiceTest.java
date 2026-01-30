package com.magiclook.mainpage;

import com.magiclook.data.Item;
import com.magiclook.data.Shop;
import com.magiclook.dto.ItemFilterDTO;
import com.magiclook.service.ItemService;
import com.magiclook.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item shirt, pants, dress;
    private Shop shop;

    @BeforeEach
    void setUp() {
        shop = new Shop("Test Shop", "Location");

        shirt = createItem("Blue Shirt", "M", "Blue", "Zara", "Cotton", "Shirt", 25.0);
        pants = createItem("Black Pants", "M", "Black", "H&M", "Denim", "Pants", 35.0);
        dress = createItem("Red Dress", "F", "Red", "Mango", "Silk", "Dress", 50.0);
    }

    @Test
    void testGetAllItems_ReturnsCompleteList() {
        when(itemRepository.findAll()).thenReturn(Arrays.asList(shirt, pants, dress));

        List<Item> result = itemService.getAllItems();

        assertThat(result).hasSize(3);
        verify(itemRepository).findAll();
    }

    @Test
    void testGetItemsByGender_ReturnsFilteredByGender() {
        when(itemRepository.findByItemTypeGender("M")).thenReturn(Arrays.asList(shirt, pants));
        when(itemRepository.findByItemTypeGender("F")).thenReturn(Arrays.asList(dress));

        assertThat(itemService.getItemsByGender("M")).hasSize(2);
        assertThat(itemService.getItemsByGender("F")).hasSize(1);
    }

    @Test
    void testGetRecentItems_ReturnsLimitedItems() {
        List<Item> allItems = Arrays.asList(shirt, pants, dress,
                createItem("Item4", "M", "White", "Brand", "Material", "Category", 10.0),
                createItem("Item5", "F", "Black", "Brand", "Material", "Category", 20.0));

        when(itemRepository.findAll()).thenReturn(allItems);

        List<Item> recent = itemService.getRecentItems(3);

        assertThat(recent)
                .hasSize(3)
                .containsExactly(shirt, pants, dress);
    }

    @Test
    void testGetDistinctFilterValues_ReturnsUniqueOptions() {
        when(itemRepository.findAllDistinctColors()).thenReturn(Arrays.asList("Blue", "Red", "Black"));
        when(itemRepository.findAllDistinctBrands()).thenReturn(Arrays.asList("Zara", "H&M", "Mango"));
        when(itemRepository.findAllDistinctMaterials()).thenReturn(Arrays.asList("Cotton", "Silk", "Denim"));
        when(itemRepository.findAllDistinctCategories()).thenReturn(Arrays.asList("Shirt", "Pants", "Dress"));

        assertThat(itemService.getAllDistinctColors()).hasSize(3);
        assertThat(itemService.getAllDistinctBrands()).hasSize(3);
        assertThat(itemService.getAllDistinctMaterials()).hasSize(3);
        assertThat(itemService.getAllDistinctCategories()).hasSize(3);
    }

    @Test
    void testFindByGenderAndFilters_WithDifferentFilterCombinations() {
        // Test 1: Filter by color only
        when(itemRepository.findByGenderAndFilters(eq("M"), any(ItemFilterDTO.class)))
                .thenReturn(Arrays.asList(shirt));

        List<Item> blueItems = itemService.findByGenderAndFilters("M", ItemFilterDTO.builder().color("Blue").build());
        assertThat(blueItems).hasSize(1);
        assertThat(blueItems.get(0).getColor()).isEqualTo("Blue");

        // Test 2: Filter by brand and category
        when(itemRepository.findByGenderAndFilters(eq("M"), any(ItemFilterDTO.class)))
                .thenReturn(Arrays.asList(shirt));

        List<Item> zaraShirts = itemService.findByGenderAndFilters("M",
                ItemFilterDTO.builder().brand("Zara").category("Shirt").build());
        assertThat(zaraShirts).hasSize(1);

        // Test 3: Filter by price range
        when(itemRepository.findByGenderAndFilters(eq("M"), any(ItemFilterDTO.class)))
                .thenReturn(Arrays.asList(pants));

        List<Item> priceFiltered = itemService.findByGenderAndFilters("M",
                ItemFilterDTO.builder().minPrice(30.0).maxPrice(40.0).build());
        assertThat(priceFiltered).hasSize(1);
        assertThat(priceFiltered.get(0).getName()).isEqualTo("Black Pants");

        // Test 4: Complex filter combination
        when(itemRepository.findByGenderAndFilters(eq("F"), any(ItemFilterDTO.class)))
                .thenReturn(Arrays.asList(dress));

        List<Item> complexFilter = itemService.findByGenderAndFilters("F",
                ItemFilterDTO.builder()
                        .color("Red")
                        .brand("Mango")
                        .material("Silk")
                        .category("Dress")
                        .minPrice(40.0)
                        .maxPrice(60.0)
                        .build());
        assertThat(complexFilter).hasSize(1);
    }

    @Test
    void testFindByGenderAndFilters_WithNullFilters_ReturnsAllGenderItems() {
        when(itemRepository.findByGenderAndFilters(eq("M"), any(ItemFilterDTO.class)))
                .thenReturn(Arrays.asList(shirt, pants));

        List<Item> allMaleItems = itemService.findByGenderAndFilters("M", new ItemFilterDTO());
        assertThat(allMaleItems).hasSize(2);
    }

    @Test
    void testGetItemsByShop_ReturnsShopSpecificItems() {
        when(itemRepository.findByShop(shop)).thenReturn(Arrays.asList(shirt, pants));

        List<Item> shopItems = itemService.getItemsByShop(shop);
        assertThat(shopItems).hasSize(2);
    }

    @Test
    void testGetRecentItems_WhenLessItemsThanLimit_ReturnsAll() {
        when(itemRepository.findAll()).thenReturn(Arrays.asList(shirt, pants));

        List<Item> recent = itemService.getRecentItems(5);
        assertThat(recent).hasSize(2);
    }

    private Item createItem(String name, String gender, String color, String brand,
            String material, String category, double price) {
        Item item = new Item();
        item.setName(name);
        item.setColor(color);
        item.setBrand(brand);
        item.setMaterial(material);
        item.setPriceRent(new BigDecimal(price));

        com.magiclook.data.ItemType itemType = new com.magiclook.data.ItemType();
        itemType.setGender(gender);
        itemType.setCategory(category);
        item.setItemType(itemType);

        return item;
    }
}