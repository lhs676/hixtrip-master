package com.hixtrip.sample.domain.inventory.repository;

/**
 *
 */
public interface InventoryRepository {

    Integer getInventoryBySkuId(String skuId);

    int addReservedInventory(String skuId, Long quantity);

    int reduceActualInventory(String skuId, Long quantity);

    int reduceReservedInventory(String skuId, Long quantity);
}
