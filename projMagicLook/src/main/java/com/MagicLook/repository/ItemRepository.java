package com.magiclook.repository;

import com.MagicLook.data.Item;
import com.MagicLook.data.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    List<Item> findByNameAndMaterialAndColorAndBrandAndSize(
        String name,
        String material,
        String color,
        String brand,
        String size
    );

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
    
    @Query("SELECT i FROM Item i WHERE i.itemType.gender = :gender " +
           "AND (:color IS NULL OR i.color = :color) " +
           "AND (:brand IS NULL OR i.brand = :brand) " +
           "AND (:material IS NULL OR i.material = :material) " +
           "AND (:category IS NULL OR i.itemType.category = :category) " +
           "AND (:minPrice IS NULL OR i.priceRent >= :minPrice) " +
           "AND (:maxPrice IS NULL OR i.priceRent <= :maxPrice)")
    List<Item> findByGenderAndFilters(
            @Param("gender") String gender,
            @Param("color") String color,
            @Param("brand") String brand,
            @Param("material") String material,
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice);
            
    List<Item> findAll();
}