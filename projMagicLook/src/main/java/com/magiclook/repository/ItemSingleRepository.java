package com.magiclook.repository;

import com.magiclook.data.ItemSingle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemSingleRepository extends JpaRepository<ItemSingle, UUID> {

    List<ItemSingle> findByItem_ItemId(Integer itemId);

    void deleteByItem_ItemIdAndSize(Integer itemId, String size);
    
    // Adicionar estas queries se necessário para melhor performance
    @Query("SELECT DISTINCT i.size FROM ItemSingle i WHERE i.item.itemId = :itemId AND i.state = 'AVAILABLE'")
    List<String> findDistinctAvailableSizesByItemId(@Param("itemId") Integer itemId);
    
    @Query("SELECT i.size, COUNT(i) FROM ItemSingle i WHERE i.item.itemId = :itemId AND i.state = 'AVAILABLE' GROUP BY i.size")
    List<Object[]> countAvailableBySize(@Param("itemId") Integer itemId);
}