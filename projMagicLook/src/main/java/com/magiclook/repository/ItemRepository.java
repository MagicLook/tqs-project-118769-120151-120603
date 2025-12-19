package com.magiclook.repository;

import com.magiclook.data.Item;
import com.magiclook.data.Shop;
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

    @Query("SELECT i FROM Item i JOIN i.shop s WHERE i.itemType.gender = :gender " +
            "AND (:color IS NULL OR i.color = :color) " +
            "AND (:brand IS NULL OR i.brand = :brand) " +
            "AND (:material IS NULL OR i.material = :material) " +
            "AND (:category IS NULL OR i.itemType.category = :category) " +
            "AND (:shopLocation IS NULL OR s.location = :shopLocation) " +
            "AND (:minPrice IS NULL OR i.priceRent >= :minPrice) " +
            "AND (:maxPrice IS NULL OR i.priceRent <= :maxPrice)")
    List<Item> findByGenderAndFilters(
            @Param("gender") String gender,
            @Param("color") String color,
            @Param("brand") String brand,
            @Param("material") String material,
            @Param("category") String category,
            @Param("shopLocation") String shopLocation,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice);

    @Query("SELECT i FROM Item i WHERE i.name = :name " +
            "AND i.material = :material " +
            "AND i.color = :color " +
            "AND i.brand = :brand " +
            "AND i.itemType.gender = :gender " +
            "AND i.itemType.category = :category " +
            "AND i.itemType.subcategory = :subcategory " +
            "AND i.shop.shopId = :shopId")
    Optional<Item> findByAllCharacteristics(
            @Param("name") String name,
            @Param("material") String material,
            @Param("color") String color,
            @Param("brand") String brand,
            @Param("gender") String gender,
            @Param("category") String category,
            @Param("subcategory") String subcategory,
            @Param("shopId") Integer shopId);

    List<Item> findAll();

    @Query("SELECT DISTINCT isg.item FROM ItemSingle isg  WHERE isg.state = :state")
    List<Item> findByItemSinglesState(@Param("state") String state);
}