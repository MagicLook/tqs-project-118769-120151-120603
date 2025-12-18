package com.magiclook.tests;

import com.magiclook.data.*;
import com.magiclook.service.*;
import com.magiclook.dto.ItemDTO;
import com.magiclook.repository.*;

import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StaffService - Comprehensive Tests")
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ItemSingleRepository itemSingleRepository;

    @Mock
    private ItemTypeRepository itemTypeRepository;

    @InjectMocks
    private StaffService staffService;

    private ItemDTO sampleDto;
    private Shop shop;
    private ItemType itemType;
    private Staff staff;
    private Item item;

    @BeforeEach
    void setUp() {
        shop = new Shop("Test Shop", "Test Location");
        shop.setShopId(1);

        itemType = new ItemType("M", "Vestido", "Curto");
        itemType.setId(1);

        staff = new Staff("Test Staff", "staff@test.com", "password123", "teststaff", shop);
        staff.setStaffId(UUID.randomUUID());

        item = new Item("Test Item", "Algodão", "Blue", "Brand",
                new BigDecimal("50.00"), new BigDecimal("200.00"), shop, itemType);
        item.setItemId(1);

        sampleDto = new ItemDTO(
                "Vestido",
                "Seda",
                "Azul",
                "Marca",
                new BigDecimal("200.00"),
                new BigDecimal("2500.00"),
                1,
                "M",
                "Vestido",
                "Curto");
    }

    // ==================== ADD ITEM TESTS ====================

    @Nested
    @DisplayName("Add Item - Success Scenarios")
    class AddItemSuccessTests {

        @Test
        @DisplayName("Should add new item when it does NOT exist")
        void addItem_whenNotExists_shouldSaveItem() {
            when(itemRepository.findByAllCharacteristics(
                    "Vestido", "Seda", "Azul", "Marca", "M", "Vestido", "Curto", 1)).thenReturn(Optional.empty());

            when(shopRepository.findById(1)).thenReturn(Optional.of(shop));
            when(itemTypeRepository.findByGenderAndCategoryAndSubcategory("M", "Vestido", "Curto"))
                    .thenReturn(itemType);
            when(itemRepository.saveAndFlush(any(Item.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            int result = staffService.addItem(sampleDto, "M");

            assertEquals(0, result);
            verify(itemRepository, times(1)).saveAndFlush(any(Item.class));
            verify(itemSingleRepository, times(1)).saveAndFlush(any(ItemSingle.class));
        }

        @Test
        @DisplayName("Should add ItemSingle when item already exists")
        void addItem_whenDuplicateExists_shouldOnlyAddItemSingle() {
            when(itemRepository.findByAllCharacteristics(
                    "Vestido", "Seda", "Azul", "Marca", "M", "Vestido", "Curto", 1)).thenReturn(Optional.of(item));

            int result = staffService.addItem(sampleDto, "M");

            assertEquals(0, result);
            verify(itemRepository, never()).saveAndFlush(any());
            verify(itemSingleRepository, times(1)).saveAndFlush(any(ItemSingle.class));
        }

        @Test
        @DisplayName("Should create ItemSingle with AVAILABLE state")
        void addItem_shouldCreateItemSingleWithAvailableState() {
            when(itemRepository.findByAllCharacteristics(anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.of(item));

            staffService.addItem(sampleDto, "L");

            verify(itemSingleRepository).saveAndFlush(argThat(itemSingle -> itemSingle.getState().equals("AVAILABLE") &&
                    itemSingle.getSize().equals("L") &&
                    itemSingle.getItem().equals(item)));
        }

        @Test
        @DisplayName("Should handle all valid sizes: XS, S, M, L, XL")
        void addItem_withAllValidSizes_shouldSucceed() {
            List<String> validSizes = Arrays.asList("XS", "S", "M", "L", "XL");

            when(itemRepository.findByAllCharacteristics(anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.of(item));

            for (String size : validSizes) {
                int result = staffService.addItem(sampleDto, size);
                assertEquals(0, result, "Size " + size + " should be valid");
            }

            verify(itemSingleRepository, times(validSizes.size())).saveAndFlush(any(ItemSingle.class));
        }

        @Test
        @DisplayName("Should handle all valid materials")
        void addItem_withAllValidMaterials_shouldSucceed() {
            List<String> validMaterials = Arrays.asList("Algodão", "Poliéster", "Seda", "Couro", "Veludo");

            when(itemRepository.findByAllCharacteristics(anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.of(item));

            for (String material : validMaterials) {
                sampleDto.setMaterial(material);
                int result = staffService.addItem(sampleDto, "M");
                assertEquals(0, result, "Material " + material + " should be valid");
            }
        }

        @Test
        @DisplayName("Should set itemId in DTO after creation")
        void addItem_shouldSetItemIdInDTO() {
            Item createdItem = new Item();
            createdItem.setItemId(999);

            when(itemRepository.findByAllCharacteristics(anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.empty());
            when(shopRepository.findById(1)).thenReturn(Optional.of(shop));
            when(itemTypeRepository.findByGenderAndCategoryAndSubcategory("M", "Vestido", "Curto"))
                    .thenReturn(itemType);
            when(itemRepository.saveAndFlush(any(Item.class))).thenReturn(createdItem);

            staffService.addItem(sampleDto, "M");

            assertEquals(999, sampleDto.getItemId());
        }

        @Test
        @DisplayName("Should preserve imagePath when provided")
        void addItem_withImagePath_shouldPreserveImagePath() {
            sampleDto.setImagePath("/uploads/test.jpg");

            when(itemRepository.findByAllCharacteristics(anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.empty());
            when(shopRepository.findById(1)).thenReturn(Optional.of(shop));
            when(itemTypeRepository.findByGenderAndCategoryAndSubcategory("M", "Vestido", "Curto"))
                    .thenReturn(itemType);
            when(itemRepository.saveAndFlush(any(Item.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            staffService.addItem(sampleDto, "M");

            verify(itemRepository).saveAndFlush(argThat(savedItem -> savedItem.getImagePath() != null &&
                    savedItem.getImagePath().equals("/uploads/test.jpg")));
        }
    }

    @Nested
    @DisplayName("Add Item - Validation Failures")
    class AddItemValidationTests {

        @Test
        @DisplayName("Should return -1 for invalid size")
        void addItem_withInvalidSize_shouldReturnMinusOne() {
            int result = staffService.addItem(sampleDto, "XXXL");

            assertEquals(-1, result);
            verify(itemRepository, never()).saveAndFlush(any());
            verify(itemSingleRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should return -1 for null size")
        void addItem_withNullSize_shouldReturnMinusOne() {
            int result = staffService.addItem(sampleDto, null);

            assertEquals(-1, result);
        }

        @Test
        @DisplayName("Should return -1 for empty size")
        void addItem_withEmptySize_shouldReturnMinusOne() {
            int result = staffService.addItem(sampleDto, "");

            assertEquals(-1, result);
        }

        @Test
        @DisplayName("Should return -2 for invalid material")
        void addItem_withInvalidMaterial_shouldReturnMinusTwo() {
            sampleDto.setMaterial("InvalidMaterial");

            int result = staffService.addItem(sampleDto, "M");

            assertEquals(-2, result);
            verify(itemRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should return -3 when shop does not exist")
        void addItem_whenShopNotFound_shouldReturnMinusThree() {
            when(itemRepository.findByAllCharacteristics(anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.empty());
            when(shopRepository.findById(1)).thenReturn(Optional.empty());

            int result = staffService.addItem(sampleDto, "M");

            assertEquals(-3, result);
            verify(itemRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should return -3 when ItemType does not exist")
        void addItem_whenItemTypeNotFound_shouldReturnMinusThree() {
            when(itemRepository.findByAllCharacteristics(anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.empty());
            when(shopRepository.findById(1)).thenReturn(Optional.of(shop));
            when(itemTypeRepository.findByGenderAndCategoryAndSubcategory("M", "Vestido", "Curto"))
                    .thenReturn(null);

            int result = staffService.addItem(sampleDto, "M");

            assertEquals(-3, result);
        }

        @Test
        @DisplayName("Should validate size before material")
        void addItem_withBothInvalidSizeAndMaterial_shouldReturnMinusOne() {
            sampleDto.setMaterial("InvalidMaterial");

            int result = staffService.addItem(sampleDto, "XXXL");

            assertEquals(-1, result); // Size validation comes first
        }
    }

    // ==================== LOGIN TESTS ====================

    @Nested
    @DisplayName("Staff Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login with valid email and password")
        void login_withValidEmail_shouldReturnStaff() {
            when(staffRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));

            Staff result = staffService.login("staff@test.com", "password123");

            assertNotNull(result);
            assertEquals(staff, result);
            verify(staffRepository).findByEmail("staff@test.com");
        }

        @Test
        @DisplayName("Should login with valid username and password")
        void login_withValidUsername_shouldReturnStaff() {
            when(staffRepository.findByEmail("teststaff")).thenReturn(Optional.empty());
            when(staffRepository.findByUsername("teststaff")).thenReturn(Optional.of(staff));

            Staff result = staffService.login("teststaff", "password123");

            assertNotNull(result);
            assertEquals(staff, result);
            verify(staffRepository).findByEmail("teststaff");
            verify(staffRepository).findByUsername("teststaff");
        }

        @Test
        @DisplayName("Should return null with wrong password via email")
        void login_withWrongPasswordViaEmail_shouldReturnNull() {
            when(staffRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));

            Staff result = staffService.login("staff@test.com", "wrongpassword");

            assertNull(result);
        }

        @Test
        @DisplayName("Should return null with wrong password via username")
        void login_withWrongPasswordViaUsername_shouldReturnNull() {
            when(staffRepository.findByEmail("teststaff")).thenReturn(Optional.empty());
            when(staffRepository.findByUsername("teststaff")).thenReturn(Optional.of(staff));

            Staff result = staffService.login("teststaff", "wrongpassword");

            assertNull(result);
        }

        @Test
        @DisplayName("Should return null when email not found")
        void login_withNonExistentEmail_shouldReturnNull() {
            when(staffRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());
            when(staffRepository.findByUsername("nonexistent@test.com")).thenReturn(Optional.empty());

            Staff result = staffService.login("nonexistent@test.com", "password123");

            assertNull(result);
        }

        @Test
        @DisplayName("Should return null when username not found")
        void login_withNonExistentUsername_shouldReturnNull() {
            when(staffRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());
            when(staffRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            Staff result = staffService.login("nonexistent", "password123");

            assertNull(result);
        }

        @Test
        @DisplayName("Should try email before username")
        void login_shouldTryEmailBeforeUsername() {
            when(staffRepository.findByEmail("teststaff")).thenReturn(Optional.empty());
            when(staffRepository.findByUsername("teststaff")).thenReturn(Optional.of(staff));

            staffService.login("teststaff", "password123");

            verify(staffRepository).findByEmail("teststaff");
            verify(staffRepository).findByUsername("teststaff");
        }

        @Test
        @DisplayName("Should not check username when email matches")
        void login_whenEmailMatches_shouldNotCheckUsername() {
            when(staffRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));

            staffService.login("staff@test.com", "password123");

            verify(staffRepository).findByEmail("staff@test.com");
            verify(staffRepository, never()).findByUsername(anyString());
        }

        @Test
        @DisplayName("Should handle null credentials gracefully")
        void login_withNullCredentials_shouldReturnNull() {
            when(staffRepository.findByEmail(null)).thenReturn(Optional.empty());
            when(staffRepository.findByUsername(null)).thenReturn(Optional.empty());

            Staff result = staffService.login(null, null);

            assertNull(result);
        }

        @Test
        @DisplayName("Should handle empty credentials gracefully")
        void login_withEmptyCredentials_shouldReturnNull() {
            when(staffRepository.findByEmail("")).thenReturn(Optional.empty());
            when(staffRepository.findByUsername("")).thenReturn(Optional.empty());

            Staff result = staffService.login("", "");

            assertNull(result);
        }
    }

    // ==================== SAVE IMAGE TESTS ====================

    @Nested
    @DisplayName("Save Image Tests")
    class SaveImageTests {

        @Mock
        private MultipartFile mockFile;
        private String testUploadDir;
        private Path testPath;

        @BeforeEach
        void setUpImageTests() throws IOException {
            testUploadDir = "uploads/test_" + System.currentTimeMillis();
            testPath = Paths.get("src/main/resources/static").toAbsolutePath().resolve(testUploadDir);
            ReflectionTestUtils.setField(staffService, "uploadDir", testUploadDir);

            if (Files.exists(testPath)) {
                Files.walk(testPath)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException ignored) {
                            }
                        });
            }
        }

        @AfterEach
        void tearDownImageTests() throws IOException {
            if (Files.exists(testPath)) {
                Files.walk(testPath)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException ignored) {
                            }
                        });
            }
        }

        @Test
        @DisplayName("Should save image successfully with valid file and itemId")
        void saveImage_withValidFileAndItemId_shouldSaveSuccessfully() throws IOException {
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("test-image.jpg");
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

            String result = staffService.saveImage(mockFile, 123);

            assertNotNull(result);
            assertTrue(result.startsWith("/"));
            assertTrue(result.contains(testUploadDir.replace("\\", "/")));
            assertTrue(result.endsWith("item_123_test-image.jpg"));
            verify(mockFile, atLeastOnce()).getInputStream();
        }

        @Test
        @DisplayName("Should save image with UUID when itemId is null")
        void saveImage_withNullItemId_shouldUseUUID() throws IOException {
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("photo.png");
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

            String result = staffService.saveImage(mockFile, null);

            assertNotNull(result);
            assertTrue(result.contains("photo.png"));
            assertTrue(result.contains("item_"));
            assertTrue(result.matches(".*/item_[a-f0-9]{8}_photo\\.png"));
            verify(mockFile, atLeastOnce()).getInputStream();
        }

        @Test
        @DisplayName("Should sanitize filename with special characters")
        void saveImage_withSpecialCharacters_shouldSanitizeFilename() throws IOException {
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("my image!@#$%^&*()+file.jpg");
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

            String result = staffService.saveImage(mockFile, 42);

            assertNotNull(result);
            String sanitized = "my image!@#$%^&*()+file.jpg".replaceAll("[^a-zA-Z0-9._-]", "_");
            assertTrue(result.contains("item_42_" + sanitized));
            assertFalse(result.contains("!"));
            verify(mockFile, atLeastOnce()).getInputStream();
        }

        @Test
        @DisplayName("Should return null when image is null")
        void saveImage_withNullImage_shouldReturnNull() throws IOException {
            String result = staffService.saveImage(null, 123);

            assertNull(result);
        }

        @Test
        @DisplayName("Should return null when image is empty")
        void saveImage_withEmptyImage_shouldReturnNull() throws IOException {
            when(mockFile.isEmpty()).thenReturn(true);

            String result = staffService.saveImage(mockFile, 123);

            assertNull(result);
            verify(mockFile, never()).getInputStream();
        }

        @Test
        @DisplayName("Should handle null original filename")
        void saveImage_withNullOriginalFilename_shouldUseDefaultName() throws IOException {
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn(null);
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

            String result = staffService.saveImage(mockFile, 99);

            assertNotNull(result);
            assertTrue(result.contains("item_99_file"));
            verify(mockFile, atLeastOnce()).getInputStream();
        }

        @Test
        @DisplayName("Should create upload directory if it doesn't exist")
        void saveImage_whenDirectoryDoesNotExist_shouldCreateIt() throws IOException {
            String newTestDir = "uploads/new_test_" + System.currentTimeMillis();
            ReflectionTestUtils.setField(staffService, "uploadDir", newTestDir);

            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

            String result = staffService.saveImage(mockFile, 1);

            assertNotNull(result);
            Path expectedDir = Paths.get("src/main/resources/static").toAbsolutePath().resolve(newTestDir);
            assertTrue(Files.exists(expectedDir));

            // Cleanup
            Files.walk(expectedDir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // ignore
                        }
                    });
        }

        @Test
        @DisplayName("Should throw IOException when file transfer fails")
        void saveImage_whenTransferFails_shouldThrowIOException() throws IOException {
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
            when(mockFile.getInputStream()).thenThrow(new IOException("Transfer failed"));

            assertThrows(IOException.class, () -> {
                staffService.saveImage(mockFile, 123);
            });
        }

        @Test
        @DisplayName("Should return path with forward slash separator")
        void saveImage_shouldReturnPathWithForwardSlash() throws IOException {
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("image.jpg");
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

            String result = staffService.saveImage(mockFile, 1);

            assertNotNull(result);
            assertTrue(result.contains("/"));
            assertFalse(result.contains("\\"));
        }
    }

    // ==================== GET ALL STAFF TESTS ====================

    @Nested
    @DisplayName("Get All Staff Tests")
    class GetAllStaffTests {

        @Test
        @DisplayName("Should return all staff members")
        void getAllStaff_shouldReturnAllStaff() {
            List<Staff> staffList = Arrays.asList(staff, staff, staff);
            when(staffRepository.findAll()).thenReturn(staffList);

            List<Staff> result = staffService.getAllStaff();

            assertEquals(3, result.size());
            verify(staffRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no staff exists")
        void getAllStaff_whenNoStaff_shouldReturnEmptyList() {
            when(staffRepository.findAll()).thenReturn(Collections.emptyList());

            List<Staff> result = staffService.getAllStaff();

            assertTrue(result.isEmpty());
            verify(staffRepository).findAll();
        }
    }

    // ==================== ENTITY TESTS ====================

    @Nested
    @DisplayName("Entity Constructor and Setter Tests")
    class EntityTests {

        @Test
        @DisplayName("Shop constructor and setters should work correctly")
        void shopConstructorAndSetters_shouldWork() {
            Shop shop1 = new Shop("Test Shop", "Test Location");
            assertEquals("Test Shop", shop1.getName());
            assertEquals("Test Location", shop1.getLocation());

            Shop shop2 = new Shop();
            shop2.setShopId(5);
            shop2.setName("Another Shop");
            shop2.setLocation("Another Location");

            assertEquals(5, shop2.getShopId());
            assertEquals("Another Shop", shop2.getName());
            assertEquals("Another Location", shop2.getLocation());
        }

        @Test
        @DisplayName("ItemType constructor and setters should work correctly")
        void itemTypeConstructorAndSetters_shouldWork() {
            ItemType type1 = new ItemType("F", "Vestido", "Curto");
            assertEquals("F", type1.getGender());
            assertEquals("Vestido", type1.getCategory());
            assertEquals("Curto", type1.getSubcategory());

            ItemType type2 = new ItemType();
            type2.setId(10);
            type2.setGender("M");
            type2.setCategory("Fato");
            type2.setSubcategory("Três peças");

            assertEquals(10, type2.getId());
            assertEquals("M", type2.getGender());
            assertEquals("Fato", type2.getCategory());
            assertEquals("Três peças", type2.getSubcategory());
        }

        @Test
        @DisplayName("Staff constructor should set all fields correctly")
        void staffConstructor_shouldSetAllFields() {
            Staff newStaff = new Staff("John Doe", "john@test.com", "pass123", "johnd", shop);

            assertEquals("John Doe", newStaff.getName());
            assertEquals("john@test.com", newStaff.getEmail());
            assertEquals("pass123", newStaff.getPassword());
            assertEquals("johnd", newStaff.getUsername());
            assertEquals(shop, newStaff.getShop());
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle special characters in item name")
        void addItem_withSpecialCharactersInName_shouldWork() {
            sampleDto.setName("Vestido & Macacão #1");

            when(itemRepository.findByAllCharacteristics(anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.of(item));

            int result = staffService.addItem(sampleDto, "M");

            assertEquals(0, result);
        }

        @Test
        @DisplayName("Should handle very large decimal prices")
        void addItem_withLargePrices_shouldWork() {
            sampleDto.setPriceRent(new BigDecimal("999999.99"));
            sampleDto.setPriceSale(new BigDecimal("9999999.99"));

            when(itemRepository.findByAllCharacteristics(anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.of(item));

            int result = staffService.addItem(sampleDto, "M");

            assertEquals(0, result);
        }

        @Test
        @DisplayName("Should handle zero prices")
        void addItem_withZeroPrices_shouldWork() {
            sampleDto.setPriceRent(BigDecimal.ZERO);
            sampleDto.setPriceSale(BigDecimal.ZERO);

            when(itemRepository.findByAllCharacteristics(anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.of(item));

            int result = staffService.addItem(sampleDto, "M");

            assertEquals(0, result);
        }

        @Test
        @DisplayName("Should handle case-sensitive size validation")
        void addItem_withLowercaseSize_shouldFail() {
            int result = staffService.addItem(sampleDto, "m");

            assertEquals(-1, result);
        }

        @Test
        @DisplayName("Should handle multiple ItemSingle creations for same Item")
        void addItem_multipleTimes_shouldCreateMultipleItemSingles() {
            when(itemRepository.findByAllCharacteristics(anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.of(item));

            staffService.addItem(sampleDto, "M");
            staffService.addItem(sampleDto, "L");
            staffService.addItem(sampleDto, "XL");

            verify(itemSingleRepository, times(3)).saveAndFlush(any(ItemSingle.class));
        }

        @Test
        @DisplayName("Login should be case-sensitive for password")
        void login_withWrongCasePassword_shouldFail() {
            when(staffRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));

            Staff result = staffService.login("staff@test.com", "PASSWORD123");

            assertNull(result);
        }
    }
}
