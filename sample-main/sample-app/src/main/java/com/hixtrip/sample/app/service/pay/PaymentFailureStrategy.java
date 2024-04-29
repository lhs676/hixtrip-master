package com.hixtrip.sample.app.service.pay;

import com.hixtrip.sample.domain.inventory.InventoryDomainService;
import com.hixtrip.sample.domain.order.OrderDomainService;
import com.hixtrip.sample.domain.order.model.Order;
import com.hixtrip.sample.domain.pay.model.CommandPay;
import org.springframework.beans.factory.annotation.Autowired;

public class PaymentFailureStrategy implements PaymentStrategy {

    private static final String status = "FAILURE";


    @Autowired
    private InventoryDomainService inventoryDomainService;

    @Autowired
    private OrderDomainService orderDomainService;

    @Override
    public void processPayment(String orderId) {

        // 支付失败处理逻辑

        Order order = orderDomainService.selectOrderByOrderId(orderId);

        if (order == null) {
            //todo
            return;
        }
        // 修改订单
        orderDomainService.orderPaySuccess(CommandPay.builder().orderId(orderId).payStatus(status).build());

        // 释放预留库存
        Boolean changeInventory = inventoryDomainService.changeInventory(order.getSkuId(), 0L, order.getAmount().longValue(), 0L);

        // 修改库存失败
        if (!changeInventory) {
            throw new RuntimeException();
        }


    }

    @Override
    public String getStatus() {
        return status;
    }
}