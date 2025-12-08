package com.MagicLook.repository;

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
    List<Item> findByShop(Shop shop);

    @Query("SELECT i FROM Item i WHERE i.itemType.gender = :gender")
    List<Item> findByItemTypeGender(@Param("gender") String gender);
    
    List<Item> findAll();
}