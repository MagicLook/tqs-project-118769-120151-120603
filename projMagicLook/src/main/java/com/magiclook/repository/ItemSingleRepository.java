package com.magiclook.repository;

import com.magiclook.data.ItemSingle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemSingleRepository extends JpaRepository<ItemSingle, UUID> {

    List<ItemSingle> findByItem_ItemId(Integer itemId);
}