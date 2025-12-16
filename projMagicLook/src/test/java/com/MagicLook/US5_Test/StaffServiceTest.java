package com.MagicLook.US5_Test;

import com.MagicLook.data.*;
import com.MagicLook.service.*;
import com.MagicLook.dto.ItemDTO;
import com.MagicLook.repository.*;

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
                "M",
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
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getContentType()).thenReturn("image/jpeg");
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
            when(mockFile.getSize()).thenReturn(2048L);
            when(mockFile.getContentType()).thenReturn("image/png");
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
            when(mockFile.getSize()).thenReturn(1024L);
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
            when(mockFile.getSize()).thenReturn(1024L);
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
            when(mockFile.getSize()).thenReturn(1024L);
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
            when(mockFile.getSize()).thenReturn(1024L);
            doThrow(new IOException("Transfer failed")).when(mockFile).transferTo(any(Path.class));

            // When & Then
            assertThrows(IOException.class, () -> {
                staffService.saveImage(mockFile, 123);
            });
        }
    }
}
