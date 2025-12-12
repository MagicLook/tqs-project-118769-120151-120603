package com.MagicLook.US5_Test;

import com.MagicLook.data.*;
import com.MagicLook.service.*;
import com.MagicLook.dto.ItemDTO;
import com.MagicLook.repository.*;

import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StaffService - addItem() tests")
class StaffServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ItemTypeRepository itemTypeRepository;

    @InjectMocks
    private StaffService staffService;

    private ItemDTO sampleDto;
    private Shop shop;
    private ItemType itemType;

    @BeforeEach
    void setUp() {
        shop = new Shop();

        shop.setShopId(1);

        itemType = new ItemType();
        itemType.setId(1);
         
        sampleDto = new ItemDTO(
                "Vestido",
                "Cetim",
                "Azul",
                "Marca",
                "M",
                new BigDecimal("200.00"),
                new BigDecimal("2500.00"),
                1,
                1 
        );
    }

    @Test
    @DisplayName("Should add item when it does NOT exist")
    void addItem_whenNotExists_shouldSaveItem() {

        // Given: no duplicate found
        when(itemRepository.findByNameAndMaterialAndColorAndBrandAndSize(
                "Vestido", "Cetim", "Azul", "Marca", "M"
        )).thenReturn(Collections.emptyList());

        // Shop and ItemType exist
        when(shopRepository.findById(1)).thenReturn(Optional.of(shop));
        when(itemTypeRepository.findById(1)).thenReturn(Optional.of(itemType));

        // saveAndFlush returns the saved entity
        when(itemRepository.saveAndFlush(any(Item.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int result = staffService.addItem(sampleDto);

        // Then
        assertEquals(0, result);
        verify(itemRepository, times(1)).saveAndFlush(any(Item.class));
    }

    @Test
    @DisplayName("Should NOT add item when duplicate exists")
    void addItem_whenDuplicateExists_shouldNotSave() {

        // Given: duplicate already in DB
        when(itemRepository.findByNameAndMaterialAndColorAndBrandAndSize(
                "Vestido", "Cetim", "Azul", "Marca", "M"
        )).thenReturn(List.of(new Item()));

        // When
        int result = staffService.addItem(sampleDto);

        // Then
        assertEquals(-1, result);
        verify(itemRepository, never()).saveAndFlush(any());
    }

}
