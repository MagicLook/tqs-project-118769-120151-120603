package com.MagicLook.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.MagicLook.data.*;

@Repository
public interface ItemTypeRepository extends JpaRepository<ItemType, Integer>{
    
}
