package com.hixtrip.sample.domain.inventory;

import com.hixtrip.sample.domain.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 库存领域服务
 * 库存设计，忽略仓库、库存品、计量单位等业务
 */
@Component
public class InventoryDomainService {

    @Autowired
    private InventoryRepository inventoryRepository;


    /**
     * 获取sku当前库存
     *
     * @param skuId
     */
    public Integer getInventory(String skuId) {
        //todo 需要你在infra实现，只需要实现缓存操作, 返回的领域对象自行定义

        Integer inventory = inventoryRepository.getInventoryBySkuId(skuId);
        return inventory;
    }

    /**
     * 修改库存
     *
     * @param skuId
     * @param sellableQuantity    可售库存
     * @param withholdingQuantity 预占库存
     * @param occupiedQuantity    占用库存
     * @return
     */
    public Boolean changeInventory(String skuId, Long sellableQuantity, Long withholdingQuantity, Long occupiedQuantity) {
        //todo 需要你在infra实现，只需要实现缓存操作。


        // 下单成功且占用库存大于 0，需要扣减实际库存跟预留内存
        if (occupiedQuantity > 0) {
            // 扣减实际库存
            if (inventoryRepository.reduceActualInventory(skuId, occupiedQuantity) > 0) {
                return true;
            }
        } else if (withholdingQuantity > 0) {
            // 扣减预留库存
            if (sellableQuantity >= 0) {
                // 扣减库存成功，直接返回结果
                if (inventoryRepository.addReservedInventory(skuId, withholdingQuantity) > 0) {
                    return true;
                }
            } else {
                // 下单失败，释放预留库存
                if (inventoryRepository.reduceReservedInventory(skuId, withholdingQuantity) > 0) {
                    return true;
                }
            }
        }
        return false;


    }
}
