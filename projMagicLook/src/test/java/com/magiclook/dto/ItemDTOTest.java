package com.magiclook.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemDTOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    private ItemDTO validDTO() {
        ItemDTO dto = new ItemDTO();
        dto.setName("Vestido Elegante");
        dto.setMaterial("Seda");
        dto.setColor("Azul");
        dto.setBrand("Zara");
        dto.setPriceRent(new BigDecimal("150.00"));
        dto.setPriceSale(new BigDecimal("3000.00"));
        dto.setShopId(1);
        dto.setGender("F");
        dto.setCategory("Vestido");
        dto.setSubcategory("Longo");
        return dto;
    }

    @Test
    @DisplayName("Valid DTO should have no violations")
    void validDTO_ShouldHaveNoViolations() {
        ItemDTO dto = validDTO();

        Set<ConstraintViolation<ItemDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), () -> "Unexpected violations: " + violations);
    }

    @Test
    @DisplayName("Constructor should set all required fields correctly")
    void constructor_ShouldSetAllFields() {
        ItemDTO dto = new ItemDTO(
            "Vestido",
            "Seda",
            "Vermelho",
            "Mango",
            new BigDecimal("200.00"),
            new BigDecimal("4000.00"),
            2,
            "F",
            "Vestido",
            "Curto"
        );

        assertEquals("Vestido", dto.getName());
        assertEquals("Seda", dto.getMaterial());
        assertEquals("Vermelho", dto.getColor());
        assertEquals("Mango", dto.getBrand());
        assertEquals(new BigDecimal("200.00"), dto.getPriceRent());
        assertEquals(new BigDecimal("4000.00"), dto.getPriceSale());
        assertEquals(2, dto.getShopId());
        assertEquals("F", dto.getGender());
        assertEquals("Vestido", dto.getCategory());
        assertEquals("Curto", dto.getSubcategory());
    }

    @Test
    @DisplayName("ShopId null should violate @NotNull")
    void shopId_Null_ShouldViolateNotNull() {
        ItemDTO dto = validDTO();
        dto.setShopId(null);

        Set<ConstraintViolation<ItemDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Deve haver violações para shopId null");
        
        boolean hasNotNullViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("shopId"));
        
        assertTrue(hasNotNullViolation, "Deve haver violação @NotNull para shopId");
    }

    @Test
    @DisplayName("Gender null should violate @NotNull")
    void gender_Null_ShouldViolateNotNull() {
        ItemDTO dto = validDTO();
        dto.setGender(null);

        Set<ConstraintViolation<ItemDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Deve haver violações para gender null");
        
        boolean hasNotNullViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("gender"));
        
        assertTrue(hasNotNullViolation, "Deve haver violação @NotNull para gender");
    }

    @Test
    @DisplayName("Category null should violate @NotNull")
    void category_Null_ShouldViolateNotNull() {
        ItemDTO dto = validDTO();
        dto.setCategory(null);

        Set<ConstraintViolation<ItemDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Deve haver violações para category null");
        
        boolean hasNotNullViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("category"));
        
        assertTrue(hasNotNullViolation, "Deve haver violação @NotNull para category");
    }

    @Test
    @DisplayName("Subcategory null should violate @NotNull")
    void subcategory_Null_ShouldViolateNotNull() {
        ItemDTO dto = validDTO();
        dto.setSubcategory(null);

        Set<ConstraintViolation<ItemDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Deve haver violações para subcategory null");
        
        boolean hasNotNullViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("subcategory"));
        
        assertTrue(hasNotNullViolation, "Deve haver violação @NotNull para subcategory");
    }

    @Test
    @DisplayName("All getters should return correct values")
    void getters_ShouldReturnCorrectValues() {
        ItemDTO dto = validDTO();
        dto.setItemId(10);
        dto.setState("AVAILABLE");
        dto.setImagePath("/uploads/item_10.jpg");

        assertEquals(10, dto.getItemId());
        assertEquals("Vestido Elegante", dto.getName());
        assertEquals("Seda", dto.getMaterial());
        assertEquals("Azul", dto.getColor());
        assertEquals("Zara", dto.getBrand());
        assertEquals("AVAILABLE", dto.getState());
        assertEquals(new BigDecimal("150.00"), dto.getPriceRent());
        assertEquals(new BigDecimal("3000.00"), dto.getPriceSale());
        assertEquals(1, dto.getShopId());
        assertEquals("F", dto.getGender());
        assertEquals("Vestido", dto.getCategory());
        assertEquals("Longo", dto.getSubcategory());
        assertEquals("/uploads/item_10.jpg", dto.getImagePath());
    }

    @Test
    @DisplayName("All setters should update values correctly")
    void setters_ShouldUpdateValuesCorrectly() {
        ItemDTO dto = new ItemDTO();
        
        dto.setItemId(20);
        dto.setName("Fato");
        dto.setMaterial("Algodão");
        dto.setColor("Preto");
        dto.setBrand("Hugo Boss");
        dto.setState("RENTED");
        dto.setPriceRent(new BigDecimal("300.00"));
        dto.setPriceSale(new BigDecimal("6000.00"));
        dto.setShopId(3);
        dto.setGender("M");
        dto.setCategory("Fato");
        dto.setSubcategory("Três peças");
        dto.setImagePath("/uploads/item_20.jpg");

        assertEquals(20, dto.getItemId());
        assertEquals("Fato", dto.getName());
        assertEquals("Algodão", dto.getMaterial());
        assertEquals("Preto", dto.getColor());
        assertEquals("Hugo Boss", dto.getBrand());
        assertEquals("RENTED", dto.getState());
        assertEquals(new BigDecimal("300.00"), dto.getPriceRent());
        assertEquals(new BigDecimal("6000.00"), dto.getPriceSale());
        assertEquals(3, dto.getShopId());
        assertEquals("M", dto.getGender());
        assertEquals("Fato", dto.getCategory());
        assertEquals("Três peças", dto.getSubcategory());
        assertEquals("/uploads/item_20.jpg", dto.getImagePath());
    }

    @Test
    @DisplayName("DTO with all null optional fields should be valid")
    void optionalFields_Null_ShouldBeValid() {
        ItemDTO dto = new ItemDTO();
        dto.setShopId(1);
        dto.setGender("F");
        dto.setCategory("Vestido");
        dto.setSubcategory("Curto");
        // name, material, color, brand, prices, state, imagePath são todos null

        Set<ConstraintViolation<ItemDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), 
            () -> "Campos opcionais null não devem causar violações: " + violations);
    }

    @Test
    @DisplayName("Price values should handle decimals correctly")
    void prices_ShouldHandleDecimalsCorrectly() {
        ItemDTO dto = validDTO();
        dto.setPriceRent(new BigDecimal("99.99"));
        dto.setPriceSale(new BigDecimal("1999.99"));

        assertEquals(new BigDecimal("99.99"), dto.getPriceRent());
        assertEquals(new BigDecimal("1999.99"), dto.getPriceSale());
    }

    @Test
    @DisplayName("Zero prices should be allowed")
    void prices_ZeroValues_ShouldBeAllowed() {
        ItemDTO dto = validDTO();
        dto.setPriceRent(BigDecimal.ZERO);
        dto.setPriceSale(BigDecimal.ZERO);

        Set<ConstraintViolation<ItemDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Preços zero devem ser permitidos");
    }

    @Test
    @DisplayName("Multiple null required fields should produce multiple violations")
    void multipleNullRequiredFields_ShouldProduceMultipleViolations() {
        ItemDTO dto = new ItemDTO();
        // Todos os campos @NotNull são null

        Set<ConstraintViolation<ItemDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Deve haver violações");
        assertEquals(4, violations.size(), 
            "Deve haver 4 violações (shopId, gender, category, subcategory)");
    }
}
