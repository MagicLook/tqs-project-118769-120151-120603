package com.MagicLook.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.MagicLook.data.*;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository <Item, Long> {
   
List<Item> findByNameAndMaterialAndColorAndBrandAndSize(
        String name,
        String material,
        String color,
        String brand,
        String size
    );

}
