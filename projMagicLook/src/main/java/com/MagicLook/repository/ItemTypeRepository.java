package com.magiclook.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.magiclook.data.*;

@Repository
public interface ItemTypeRepository extends JpaRepository<ItemType, Integer>{

    public ItemType findByGenderAndCategoryAndSubcategory(String gender, String category, String subcategory);

}
