package com.magiclook.repository;

import com.MagicLook.data.ItemSingle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ItemSingleRepository extends JpaRepository<ItemSingle, UUID> {
    
}