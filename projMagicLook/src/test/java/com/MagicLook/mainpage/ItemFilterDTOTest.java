package com.magiclook.mainpage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.MagicLook.dto.ItemFilterDTO;

import static org.assertj.core.api.Assertions.assertThat;

class ItemFilterDTOTest {

    @Test
    void testDefaultConstructor_CreatesEmptyFilter() {
        ItemFilterDTO filter = new ItemFilterDTO();
        
        assertThat(filter.getColor()).isNull();
        assertThat(filter.getBrand()).isNull();
        assertThat(filter.getMaterial()).isNull();
        assertThat(filter.getCategory()).isNull();
        assertThat(filter.getMinPrice()).isNull();
        assertThat(filter.getMaxPrice()).isNull();
        assertThat(filter.hasFilters()).isFalse();
    }

    @Test
    void testArgsConstructor_CreatesFilterWithAllValues() {
        ItemFilterDTO filter = new ItemFilterDTO("Blue", "Zara", "Cotton", "Shirt", 20.0, 50.0);
        
        assertThat(filter.getColor()).isEqualTo("Blue");
        assertThat(filter.getBrand()).isEqualTo("Zara");
        assertThat(filter.getMaterial()).isEqualTo("Cotton");
        assertThat(filter.getCategory()).isEqualTo("Shirt");
        assertThat(filter.getMinPrice()).isEqualTo(20.0);
        assertThat(filter.getMaxPrice()).isEqualTo(50.0);
        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    void testSettersAndGetters_WorkCorrectly() {
        ItemFilterDTO filter = new ItemFilterDTO();
        
        filter.setColor("Red");
        filter.setBrand("H&M");
        filter.setMaterial("Silk");
        filter.setCategory("Dress");
        filter.setMinPrice(30.0);
        filter.setMaxPrice(60.0);
        
        assertThat(filter.getColor()).isEqualTo("Red");
        assertThat(filter.getBrand()).isEqualTo("H&M");
        assertThat(filter.getMaterial()).isEqualTo("Silk");
        assertThat(filter.getCategory()).isEqualTo("Dress");
        assertThat(filter.getMinPrice()).isEqualTo(30.0);
        assertThat(filter.getMaxPrice()).isEqualTo(60.0);
        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    void testHasFilters_WithColor_ReturnsTrue() {
        ItemFilterDTO filter = new ItemFilterDTO();
        filter.setColor("Blue");
        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    void testHasFilters_WithBrand_ReturnsTrue() {
        ItemFilterDTO filter = new ItemFilterDTO();
        filter.setBrand("Zara");
        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    void testHasFilters_WithMaterial_ReturnsTrue() {
        ItemFilterDTO filter = new ItemFilterDTO();
        filter.setMaterial("Cotton");
        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    void testHasFilters_WithCategory_ReturnsTrue() {
        ItemFilterDTO filter = new ItemFilterDTO();
        filter.setCategory("Shirt");
        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    void testHasFilters_WithMinPrice_ReturnsTrue() {
        ItemFilterDTO filter = new ItemFilterDTO();
        filter.setMinPrice(10.0);
        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    void testHasFilters_WithMaxPrice_ReturnsTrue() {
        ItemFilterDTO filter = new ItemFilterDTO();
        filter.setMaxPrice(100.0);
        assertThat(filter.hasFilters()).isTrue();
    }

    @Test
    void testHasFilters_WithEmptyFilter_ReturnsFalse() {
        ItemFilterDTO filter = new ItemFilterDTO();
        assertThat(filter.hasFilters()).isFalse();
    }

    @Test
    void testHasFilters_WithMultipleFilters_ReturnsTrue() {
        ItemFilterDTO filter = new ItemFilterDTO("Blue", "Zara", null, null, 20.0, null);
        assertThat(filter.hasFilters()).isTrue();
    }
}