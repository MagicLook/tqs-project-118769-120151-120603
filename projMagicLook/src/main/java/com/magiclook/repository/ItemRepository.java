package com.magiclook.repository;

import com.magiclook.data.Item;
import com.magiclook.data.Shop;
import com.magiclook.dto.ItemFilterDTO;
import com.magiclook.dto.ItemDTO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
        List<Item> findByNameAndMaterialAndColorAndBrand(
                        String name,
                        String material,
                        String color,
                        String brand);

        List<Item> findByShop(Shop shop);

        @Query("SELECT i FROM Item i WHERE i.itemType.gender = :gender")
        List<Item> findByItemTypeGender(@Param("gender") String gender);

        @Query("SELECT DISTINCT i.color FROM Item i")
        List<String> findAllDistinctColors();

        @Query("SELECT DISTINCT i.brand FROM Item i")
        List<String> findAllDistinctBrands();

        @Query("SELECT DISTINCT i.material FROM Item i")
        List<String> findAllDistinctMaterials();

        @Query("SELECT DISTINCT i.itemType.category FROM Item i")
        List<String> findAllDistinctCategories();

        @Query("SELECT DISTINCT s.location FROM Item i JOIN i.shop s")
        List<String> findAllDistinctShopLocations();

        @Query("SELECT DISTINCT i FROM Item i " +
                        "JOIN i.itemSingles isg " +
                        "WHERE i.itemType.gender = :gender " +
                        "AND (:#{#filter.color} IS NULL OR i.color = :#{#filter.color}) " +
                        "AND (:#{#filter.brand} IS NULL OR i.brand = :#{#filter.brand}) " +
                        "AND (:#{#filter.material} IS NULL OR i.material = :#{#filter.material}) " +
                        "AND (:#{#filter.category} IS NULL OR i.itemType.category = :#{#filter.category}) " +
                        "AND (:#{#filter.subcategory} IS NULL OR i.itemType.subcategory = :#{#filter.subcategory}) " +
                        "AND (:#{#filter.size} IS NULL OR isg.size = :#{#filter.size}) " +
                        "AND isg.state = 'AVAILABLE' " +
                        "AND (:#{#filter.shopLocation} IS NULL OR i.shop.location = :#{#filter.shopLocation}) " +
                        "AND (:#{#filter.minPrice} IS NULL OR i.priceRent >= :#{#filter.minPrice}) " +
                        "AND (:#{#filter.maxPrice} IS NULL OR i.priceRent <= :#{#filter.maxPrice})")
        List<Item> findByGenderAndFilters(
                        @Param("gender") String gender,
                        @Param("filter") ItemFilterDTO filter);

        @Query("SELECT i FROM Item i WHERE i.name = :#{#itemDTO.name} " +
                        "AND i.material = :#{#itemDTO.material} " +
                        "AND i.color = :#{#itemDTO.color} " +
                        "AND i.brand = :#{#itemDTO.brand} " +
                        "AND i.itemType.gender = :#{#itemDTO.gender} " +
                        "AND i.itemType.category = :#{#itemDTO.category} " +
                        "AND i.itemType.subcategory = :#{#itemDTO.subcategory} " +
                        "AND i.shop.shopId = :#{#itemDTO.shopId}")
        Optional<Item> findByAllCharacteristics(
                        @Param("itemDTO") ItemDTO itemDTO);

        List<Item> findAll();

        @Query("SELECT DISTINCT isg.item FROM ItemSingle isg  WHERE isg.state = :state")
        List<Item> findByItemSinglesState(@Param("state") String state);

        // Adicione estes métodos para obter valores distintos para os filtros
        @Query("SELECT DISTINCT i.itemType.subcategory FROM Item i WHERE i.itemType.gender = :gender")
        List<String> findAllDistinctSubcategoriesByGender(@Param("gender") String gender);

        @Query("SELECT DISTINCT isg.size FROM ItemSingle isg " +
                        "JOIN isg.item i " +
                        "WHERE i.itemType.gender = :gender " +
                        "AND isg.state = 'AVAILABLE' " +
                        "ORDER BY isg.size")
        List<String> findAllDistinctSizesByGender(@Param("gender") String gender);
}