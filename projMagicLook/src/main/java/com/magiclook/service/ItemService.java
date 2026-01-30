package com.magiclook.service;

import com.magiclook.dto.ItemFilterDTO;
import com.magiclook.data.Item;
import com.magiclook.data.ItemSingle;
import com.magiclook.data.Shop;
import com.magiclook.repository.ItemRepository;
import com.magiclook.repository.ItemSingleRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;
    private final ItemSingleRepository itemSingleRepository;

    public ItemService(ItemRepository itemRepository, ItemSingleRepository itemSingleRepository) {
        this.itemRepository = itemRepository;
        this.itemSingleRepository = itemSingleRepository;
    }

    public List<Item> getItemsByShop(Shop shop) {
        if (shop == null) {
            logger.warn("getItemsByShop called with null shop");
            return List.of();
        }
        return itemRepository.findByShop(shop);
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public List<Item> getItemsByGender(String gender) {
        if (gender == null || gender.isEmpty()) {
            logger.warn("getItemsByGender called with null or empty gender");
            return List.of();
        }
        return itemRepository.findByItemTypeGender(gender);
    }

    public List<Item> getRecentItems(int limit) {
        return itemRepository.findAll().stream()
                .limit(limit)
                .toList();
    }

    public List<String> getAllDistinctColors() {
        return itemRepository.findAllDistinctColors();
    }

    public List<String> getAllDistinctBrands() {
        return itemRepository.findAllDistinctBrands();
    }

    public List<String> getAllDistinctMaterials() {
        return itemRepository.findAllDistinctMaterials();
    }

    public List<String> getAllDistinctCategories() {
        return itemRepository.findAllDistinctCategories();
    }

    public List<String> getAllDistinctShopLocations() {
        return itemRepository.findAllDistinctShopLocations();
    }

    public List<Item> findByGenderAndFilters(String gender, ItemFilterDTO filter) {
        if (gender == null || gender.isEmpty() || filter == null) {
            logger.warn("findByGenderAndFilters called with invalid parameters");
            return List.of();
        }

        // Sanitize filter: convert empty strings to nulls
        sanitizeFilter(filter);

        return itemRepository.findByGenderAndFilters(gender, filter);
    }

    private void sanitizeFilter(ItemFilterDTO filter) {
        if (filter.getColor() != null && filter.getColor().isEmpty())
            filter.setColor(null);
        if (filter.getBrand() != null && filter.getBrand().isEmpty())
            filter.setBrand(null);
        if (filter.getMaterial() != null && filter.getMaterial().isEmpty())
            filter.setMaterial(null);
        if (filter.getCategory() != null && filter.getCategory().isEmpty())
            filter.setCategory(null);
        if (filter.getSubcategory() != null && filter.getSubcategory().isEmpty())
            filter.setSubcategory(null);
        if (filter.getShopLocation() != null && filter.getShopLocation().isEmpty())
            filter.setShopLocation(null);
        if (filter.getSize() != null && filter.getSize().isEmpty())
            filter.setSize(null);
    }

    public List<Item> getAllItemsByState(String state) {
        return itemRepository.findByItemSinglesState(state);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public List<ItemSingle> getItems(Integer itemId) {
        if (itemId == null) {
            logger.warn("getItems called with null itemId");
            return List.of();
        }
        return itemSingleRepository.findByItem_ItemId(itemId);
    }

    public Optional<Item> getItemById(Integer itemId) {
        if (itemId == null) {
            logger.warn("getItemById called with null itemId");
            return Optional.empty();
        }
        return itemRepository.findById(itemId);
    }

    // Método simplificado para obter tamanhos disponíveis
    public List<String> getAvailableSizesForItem(Integer itemId) {
        if (itemId == null) {
            logger.warn("getAvailableSizesForItem called with null itemId");
            return List.of();
        }
        return itemSingleRepository.findByItem_ItemId(itemId)
                .stream()
                .filter(is -> "AVAILABLE".equals(is.getState()))
                .map(ItemSingle::getSize)
                .distinct()
                .sorted()
                .toList();
    }

    public List<String> getAllDistinctSubcategoriesByGender(String gender) {
        if (gender == null || gender.isEmpty()) {
            logger.warn("getAllDistinctSubcategoriesByGender called with null or empty gender");
            return List.of();
        }
        return itemRepository.findAllDistinctSubcategoriesByGender(gender);
    }

    public List<String> getAllDistinctSizesByGender(String gender) {
        if (gender == null || gender.isEmpty()) {
            logger.warn("getAllDistinctSizesByGender called with null or empty gender");
            return List.of();
        }
        return itemRepository.findAllDistinctSizesByGender(gender);
    }

}