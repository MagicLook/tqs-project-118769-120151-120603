package com.magiclook.US5_Test;

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

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Mock
    private ItemSingleRepository itemSingleRepository;

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
                "Seda",
                "Azul",
                "Marca",
                new BigDecimal("200.00"),
                new BigDecimal("2500.00"),
                1,
                "M",
                "Vestido",
                "Curto" 
        );
    }

    @Test
    @DisplayName("Should add item when it does NOT exist")
    void addItem_whenNotExists_shouldSaveItem() {

        // Given: no duplicate found
        when(itemRepository.findByAllCharacteristics(
                "Vestido",
                "Seda",
                "Azul",
                "Marca",
                "M",
                "Vestido",
                "Curto",
                1
                
        )).thenReturn(Optional.empty());

        // Shop and ItemType exist
        when(shopRepository.findById(1)).thenReturn(Optional.of(shop));
        when(itemTypeRepository.findByGenderAndCategoryAndSubcategory("M", "Vestido", "Curto")).thenReturn(itemType);

        // saveAndFlush returns the saved entity
        when(itemRepository.saveAndFlush(any(Item.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int result = staffService.addItem(sampleDto, "M");

        // Then
        assertEquals(0, result);
        verify(itemRepository, times(1)).saveAndFlush(any(Item.class));
    }

    @Test
    @DisplayName("Should NOT add item when duplicate exists")
    void addItem_whenDuplicateExists_shouldNotSave() {

        // Given: duplicate already in DB
        when(itemRepository.findByAllCharacteristics(
                "Vestido",
                "Seda",
                "Azul",
                "Marca",
                "M",
                "Vestido",
                "Curto",
                1
              
        )).thenReturn(Optional.of(new Item()));

        // When
        int result = staffService.addItem(sampleDto, "M");

        // Then
        assertEquals(0, result);
        verify(itemRepository, never()).saveAndFlush(any());
    }

    @Nested
    @DisplayName("saveImage() tests")
    class SaveImageTests {

        @Mock
        private MultipartFile mockFile;

        private String testUploadDir;

        @BeforeEach
        void setUpImageTests() throws IOException {
            // Create a temporary test directory
            testUploadDir = "uploads/test_" + System.currentTimeMillis();
            ReflectionTestUtils.setField(staffService, "uploadDir", testUploadDir);
        }

        @AfterEach
        void cleanUpImageTests() throws IOException {
            // Clean up test directory
            Path testPath = Paths.get(testUploadDir);
            if (Files.exists(testPath)) {
                Files.walk(testPath)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // ignore cleanup errors
                        }
                    });
            }
        }

        @Test
        @DisplayName("Should save image successfully with valid file and itemId")
        void saveImage_withValidFileAndItemId_shouldSaveSuccessfully() throws IOException {
            // Given
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("test-image.jpg");
            doNothing().when(mockFile).transferTo(any(Path.class));

            // When
            String result = staffService.saveImage(mockFile, 123);

            // Then
            assertNotNull(result);
            assertTrue(result.contains("item_123_test-image.jpg"));
            assertTrue(result.startsWith(testUploadDir));
            verify(mockFile, times(1)).transferTo(any(Path.class));
        }

        @Test
        @DisplayName("Should save image with UUID when itemId is null")
        void saveImage_withNullItemId_shouldUseUUID() throws IOException {
            // Given
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("photo.png");
            doNothing().when(mockFile).transferTo(any(Path.class));

            // When
            String result = staffService.saveImage(mockFile, null);

            // Then
            assertNotNull(result);
            assertTrue(result.contains("photo.png"));
            assertTrue(result.contains("item_"));
            // Should contain UUID (8 chars) before the filename
            assertTrue(result.matches(".*item_[a-f0-9-]{8}_photo\\.png"));
            verify(mockFile, times(1)).transferTo(any(Path.class));
        }

        @Test
        @DisplayName("Should sanitize filename with special characters")
        void saveImage_withSpecialCharacters_shouldSanitizeFilename() throws IOException {
            // Given
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("my image!@#$%^&*()+file.jpg");
            doNothing().when(mockFile).transferTo(any(Path.class));

            // When
            String result = staffService.saveImage(mockFile, 42);

            // Then
            assertNotNull(result);
            // Special characters should be replaced with underscores
            assertTrue(result.contains("item_42_my_image___________file.jpg"));
            verify(mockFile, times(1)).transferTo(any(Path.class));
        }

        @Test
        @DisplayName("Should return null when image is null")
        void saveImage_withNullImage_shouldReturnNull() throws IOException {
            // When
            String result = staffService.saveImage(null, 123);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should return null when image is empty")
        void saveImage_withEmptyImage_shouldReturnNull() throws IOException {
            // Given
            when(mockFile.isEmpty()).thenReturn(true);

            // When
            String result = staffService.saveImage(mockFile, 123);

            // Then
            assertNull(result);
            verify(mockFile, never()).transferTo(any(Path.class));
        }

        @Test
        @DisplayName("Should handle null original filename")
        void saveImage_withNullOriginalFilename_shouldUseDefaultName() throws IOException {
            // Given
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn(null);
            doNothing().when(mockFile).transferTo(any(Path.class));

            // When
            String result = staffService.saveImage(mockFile, 99);

            // Then
            assertNotNull(result);
            assertTrue(result.contains("item_99_file"));
            verify(mockFile, times(1)).transferTo(any(Path.class));
        }

        @Test
        @DisplayName("Should create upload directory if it doesn't exist")
        void saveImage_whenDirectoryDoesNotExist_shouldCreateIt() throws IOException {
            // Given
            String newTestDir = "uploads/new_test_" + System.currentTimeMillis();
            ReflectionTestUtils.setField(staffService, "uploadDir", newTestDir);
            
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
            doNothing().when(mockFile).transferTo(any(Path.class));

            // When
            String result = staffService.saveImage(mockFile, 1);

            // Then
            assertNotNull(result);
            assertTrue(Files.exists(Paths.get(newTestDir)));
            
            // Cleanup
            Files.walk(Paths.get(newTestDir))
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
            // Given
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
            doThrow(new IOException("Transfer failed")).when(mockFile).transferTo(any(Path.class));

            // When & Then
            assertThrows(IOException.class, () -> {
                staffService.saveImage(mockFile, 123);
            });
        }
    }

    @Test
    @DisplayName("Shop constructor and setters should work correctly")
    void shopConstructorAndSetters_shouldWork() {
        // Test parameterized constructor
        Shop shop1 = new Shop("Test Shop", "Test Location");
        assertEquals("Test Shop", shop1.getName());
        assertEquals("Test Location", shop1.getLocation());
        
        // Test default constructor + setters
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
        // Test parameterized constructor
        ItemType type1 = new ItemType("F", "Vestido", "Curto");
        assertEquals("F", type1.getGender());
        assertEquals("Vestido", type1.getCategory());
        assertEquals("Curto", type1.getSubcategory());
        
        // Test default constructor + setters
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
}
