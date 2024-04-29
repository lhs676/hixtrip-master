package com.hixtrip.sample.app.service.pay;

import com.hixtrip.sample.domain.inventory.InventoryDomainService;
import com.hixtrip.sample.domain.order.OrderDomainService;
import com.hixtrip.sample.domain.order.model.Order;
import com.hixtrip.sample.domain.pay.model.CommandPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentSuccessStrategy implements PaymentStrategy {

    private static final String status = "SUCCESS";

    @Autowired
    private InventoryDomainService inventoryDomainService;

    @Autowired
    private OrderDomainService orderDomainService;

    @Override
    @Transactional
    public void processPayment(String orderId) {

        // 处理支付成功逻辑
        Order order = orderDomainService.selectOrderByOrderId(orderId);
        if (order == null) {
            //todo
            return;
        }


        // 修改订单
        orderDomainService.orderPaySuccess(CommandPay.builder().orderId(orderId).payStatus(status).build());

        // 修改库存
        Boolean changeInventory = inventoryDomainService.changeInventory(order.getSkuId(), 0L, 0L, order.getAmount().longValue());

        // 修改库存失败
        if (!changeInventory){
            throw new RuntimeException();
        }

    }

    @Override
    public String getStatus() {
        return status;
    }


}