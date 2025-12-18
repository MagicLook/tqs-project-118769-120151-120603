package com.magiclook.services;

import com.magiclook.data.*;
import com.magiclook.dto.ItemDTO;
import com.magiclook.repository.*;
import com.magiclook.service.StaffService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemSingleRepository itemSingleRepository;

    @Mock
    private ItemTypeRepository itemTypeRepository;

    @InjectMocks
    private StaffService staffService;

    private Staff testStaff;
    private Shop testShop;
    private Item testItem;
    private ItemSingle testItemSingle;
    private ItemType testItemType;

    @BeforeEach
    void setUp() {
        testShop = new Shop();
        testShop.setShopId(1);
        testShop.setName("Loja Teste");
        testShop.setLocation("Localização Teste");

        testStaff = new Staff();
        testStaff.setStaffId(UUID.randomUUID());
        testStaff.setName("Staff Teste");
        testStaff.setEmail("staff@test.com");
        testStaff.setUsername("staffuser");
        testStaff.setPassword("password123");
        testStaff.setShop(testShop);

        testItemType = new ItemType();
        testItemType.setGender("F");
        testItemType.setCategory("Vestido");
        testItemType.setSubcategory("Longo");

        testItem = new Item();
        testItem.setItemId(1);
        testItem.setName("Vestido Azul");
        testItem.setBrand("Zara");
        testItem.setMaterial("Seda");
        testItem.setColor("Azul");
        testItem.setPriceRent(new BigDecimal("50.00"));
        testItem.setPriceSale(new BigDecimal("200.00"));
        testItem.setShop(testShop);
        testItem.setItemType(testItemType);

        testItemSingle = new ItemSingle();
        testItemSingle.setItem(testItem);
        testItemSingle.setSize("M");
        testItemSingle.setState("AVAILABLE");
    }

    @Test
    void testLogin_WithValidEmail_ShouldReturnStaff() {
        String email = "staff@test.com";
        String password = "password123";
        
        when(staffRepository.findByEmail(email)).thenReturn(Optional.of(testStaff));

        Staff result = staffService.login(email, password);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(password, result.getPassword());
        verify(staffRepository).findByEmail(email);
        verify(staffRepository, never()).findByUsername(anyString());
    }

    @Test
    void testLogin_WithValidUsername_ShouldReturnStaff() {
        String username = "staffuser";
        String password = "password123";
        
        when(staffRepository.findByEmail(username)).thenReturn(Optional.empty());
        when(staffRepository.findByUsername(username)).thenReturn(Optional.of(testStaff));

        Staff result = staffService.login(username, password);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(password, result.getPassword());
        verify(staffRepository).findByEmail(username);
        verify(staffRepository).findByUsername(username);
    }

    @Test
    void testLogin_WithInvalidCredentials_ShouldReturnNull() {
        String username = "wronguser";
        String password = "wrongpass";
        
        when(staffRepository.findByEmail(username)).thenReturn(Optional.empty());
        when(staffRepository.findByUsername(username)).thenReturn(Optional.empty());

        Staff result = staffService.login(username, password);

        assertNull(result);
        verify(staffRepository).findByEmail(username);
        verify(staffRepository).findByUsername(username);
    }

    @Test
    void testLogin_WithWrongPassword_ShouldReturnNull() {
        String email = "staff@test.com";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        
        testStaff.setPassword(correctPassword);
        when(staffRepository.findByEmail(email)).thenReturn(Optional.of(testStaff));
        when(staffRepository.findByUsername(email)).thenReturn(Optional.empty());

        Staff result = staffService.login(email, wrongPassword);

        assertNull(result);
        verify(staffRepository).findByEmail(email);
        verify(staffRepository).findByUsername(email);
    }

    // ==================== DELETE ITEM SIZE TESTS ====================

    @Test
    void testDeleteItemSize_WithExistingSize_ShouldDeleteSize() {
        Integer itemId = 1;
        String size = "M";
        List<ItemSingle> remainingSingles = new ArrayList<>();
        remainingSingles.add(new ItemSingle("AVAILABLE", testItem, "L"));

        when(itemSingleRepository.findByItem_ItemId(itemId)).thenReturn(remainingSingles);

        staffService.deleteItemSize(itemId, size);

        verify(itemSingleRepository).deleteByItem_ItemIdAndSize(itemId, size);
        verify(itemSingleRepository).findByItem_ItemId(itemId);
        verify(itemRepository, never()).deleteById(itemId);
    }

    @Test
    void testDeleteItemSize_WhenLastSize_ShouldDeleteItem() {
        Integer itemId = 1;
        String size = "M";

        when(itemSingleRepository.findByItem_ItemId(itemId)).thenReturn(new ArrayList<>());

        staffService.deleteItemSize(itemId, size);

        verify(itemSingleRepository).deleteByItem_ItemIdAndSize(itemId, size);
        verify(itemSingleRepository).findByItem_ItemId(itemId);
        verify(itemRepository).deleteById(itemId);
    }

    // ==================== UPDATE ITEM SINGLE TESTS ====================

    @Test
    void testUpdateItemSingle_WithValidSizeAndState_ShouldUpdate() {
        UUID id = UUID.randomUUID();
        String newSize = "L";
        String newState = "RENTED";

        testItemSingle.setSize("M");
        testItemSingle.setState("AVAILABLE");

        when(itemSingleRepository.findById(id)).thenReturn(Optional.of(testItemSingle));

        staffService.updateItemSingle(id, newSize, newState);

        assertEquals(newSize, testItemSingle.getSize());
        assertEquals(newState, testItemSingle.getState());
        verify(itemSingleRepository).findById(id);
        verify(itemSingleRepository).saveAndFlush(testItemSingle);
    }

    @Test
    void testUpdateItemSingle_WithOnlySize_ShouldUpdateSize() {
        UUID id = UUID.randomUUID();
        String newSize = "XL";

        testItemSingle.setSize("M");
        testItemSingle.setState("AVAILABLE");

        when(itemSingleRepository.findById(id)).thenReturn(Optional.of(testItemSingle));

        staffService.updateItemSingle(id, newSize, null);

        assertEquals(newSize, testItemSingle.getSize());
        assertEquals("AVAILABLE", testItemSingle.getState());
        verify(itemSingleRepository).saveAndFlush(testItemSingle);
    }

    @Test
    void testUpdateItemSingle_WithOnlyState_ShouldUpdateState() {
        UUID id = UUID.randomUUID();
        String newState = "LAUNDRY";

        testItemSingle.setSize("M");
        testItemSingle.setState("AVAILABLE");

        when(itemSingleRepository.findById(id)).thenReturn(Optional.of(testItemSingle));

        staffService.updateItemSingle(id, null, newState);

        assertEquals("M", testItemSingle.getSize());
        assertEquals(newState, testItemSingle.getState());
        verify(itemSingleRepository).saveAndFlush(testItemSingle);
    }

    @Test
    void testUpdateItemSingle_WithBlankValues_ShouldNotUpdate() {
        UUID id = UUID.randomUUID();

        testItemSingle.setSize("M");
        testItemSingle.setState("AVAILABLE");

        when(itemSingleRepository.findById(id)).thenReturn(Optional.of(testItemSingle));

        staffService.updateItemSingle(id, "", "");

        assertEquals("M", testItemSingle.getSize());
        assertEquals("AVAILABLE", testItemSingle.getState());
        verify(itemSingleRepository, never()).saveAndFlush(any());
    }

    @Test
    void testUpdateItemSingle_WithNonExistentId_ShouldDoNothing() {
        UUID id = UUID.randomUUID();

        when(itemSingleRepository.findById(id)).thenReturn(Optional.empty());

        staffService.updateItemSingle(id, "L", "RENTED");

        verify(itemSingleRepository).findById(id);
        verify(itemSingleRepository, never()).saveAndFlush(any());
    }

    // ==================== UPDATE ITEM TESTS ====================

    @Test
    void testUpdateItem_WithValidData_ShouldUpdateItem() {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setItemId(1);
        itemDTO.setName("Vestido Rosa");
        itemDTO.setBrand("Mango");
        itemDTO.setMaterial("Algodão");
        itemDTO.setColor("Rosa");
        itemDTO.setPriceRent(new BigDecimal("60.00"));
        itemDTO.setPriceSale(new BigDecimal("250.00"));

        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));

        int result = staffService.updateItem(itemDTO);

        assertEquals(0, result);
        assertEquals("Vestido Rosa", testItem.getName());
        assertEquals("Mango", testItem.getBrand());
        assertEquals("Algodão", testItem.getMaterial());
        assertEquals("Rosa", testItem.getColor());
        assertEquals(new BigDecimal("60.00"), testItem.getPriceRent());
        assertEquals(new BigDecimal("250.00"), testItem.getPriceSale());
        verify(itemRepository).save(testItem);
    }

    @Test
    void testUpdateItem_WithNonExistentItem_ShouldReturnMinusOne() {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setItemId(999);

        when(itemRepository.findById(999)).thenReturn(Optional.empty());

        int result = staffService.updateItem(itemDTO);

        assertEquals(-1, result);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testUpdateItem_WithItemType_ShouldUpdateItemType() {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setItemId(1);
        itemDTO.setGender("M");
        itemDTO.setCategory("Fato");
        itemDTO.setSubcategory("Simples");

        ItemType newItemType = new ItemType();
        newItemType.setGender("M");
        newItemType.setCategory("Fato");
        newItemType.setSubcategory("Simples");

        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(itemTypeRepository.findByGenderAndCategoryAndSubcategory("M", "Fato", "Simples"))
                .thenReturn(newItemType);

        int result = staffService.updateItem(itemDTO);

        assertEquals(0, result);
        assertEquals(newItemType, testItem.getItemType());
        verify(itemRepository).save(testItem);
    }

    @Test
    void testUpdateItem_WithNoChanges_ShouldNotSave() {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setItemId(1);
        itemDTO.setName("Vestido Azul");
        itemDTO.setBrand("Zara");
        itemDTO.setMaterial("Seda");
        itemDTO.setColor("Azul");
        itemDTO.setPriceRent(new BigDecimal("50.00"));
        itemDTO.setPriceSale(new BigDecimal("200.00"));

        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));

        int result = staffService.updateItem(itemDTO);

        assertEquals(0, result);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testUpdateItem_WithPartialUpdate_ShouldUpdateOnlyChangedFields() {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setItemId(1);
        itemDTO.setName("Vestido Verde");
        itemDTO.setPriceRent(new BigDecimal("55.00"));

        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));

        int result = staffService.updateItem(itemDTO);

        assertEquals(0, result);
        assertEquals("Vestido Verde", testItem.getName());
        assertEquals(new BigDecimal("55.00"), testItem.getPriceRent());
        assertEquals("Zara", testItem.getBrand());
        assertEquals("Seda", testItem.getMaterial());
        assertEquals("Azul", testItem.getColor());
        verify(itemRepository).save(testItem);
    }
}