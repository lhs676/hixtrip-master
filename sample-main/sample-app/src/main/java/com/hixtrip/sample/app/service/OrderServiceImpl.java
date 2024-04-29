package com.hixtrip.sample.app.service;

import com.hixtrip.sample.app.api.OrderService;
import com.hixtrip.sample.client.order.dto.CommandOderCreateDTO;
import com.hixtrip.sample.client.order.dto.vo.OrderCreateVO;
import com.hixtrip.sample.domain.commodity.CommodityDomainService;
import com.hixtrip.sample.domain.inventory.InventoryDomainService;
import com.hixtrip.sample.domain.order.OrderDomainService;
import com.hixtrip.sample.domain.order.model.Order;
import com.hixtrip.sample.domain.pay.PayDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * app层负责处理request请求，调用领域服务
 */
@Component
public class OrderServiceImpl implements OrderService {

    // 待支付
    private static final String PENDING = "Pending";
    //支付中
    private static final String PROCESSING = "Processing";
    //支付成功
    private static final String SUCCESS = "Success";
    //支付失败
    private static final String FAILED = "Failed";


    @Autowired
    private InventoryDomainService inventoryDomainService;



    @Autowired
    private OrderDomainService orderDomainService;

    @Autowired
    private CommodityDomainService commodityDomainService;

    @Override
    public OrderCreateVO placeOrder(CommandOderCreateDTO commandOderCreateDTO) {


        // 1. 扣减库存（仅在缓存中实现）
        Integer inventory = inventoryDomainService.getInventory(commandOderCreateDTO.getSkuId());

        if (commandOderCreateDTO.getAmount() > inventory) {
            return OrderCreateVO.builder().code("10001").msg("库存不足").build();
        }

        boolean inventoryDecreased = inventoryDomainService.changeInventory(commandOderCreateDTO.getSkuId(), inventory.longValue(), commandOderCreateDTO.getAmount().longValue(),
                0l);

        if (!inventoryDecreased) {
            return OrderCreateVO.builder().code("10001").msg("库存不足").build();
        }

        BigDecimal skuPrice = commodityDomainService.getSkuPrice(commandOderCreateDTO.getSkuId());

        // 订单金额
        BigDecimal arderAmount = skuPrice.multiply(BigDecimal.valueOf(commandOderCreateDTO.getAmount()));
        Order order = Order.builder().userId(commandOderCreateDTO.getUserId()).skuId(commandOderCreateDTO.getSkuId())
                .amount(commandOderCreateDTO.getAmount()).money(arderAmount).payStatus(PENDING).delFlag(0L)
                .createBy(commandOderCreateDTO.getUserId()).createTime(LocalDateTime.now()).
                updateBy(commandOderCreateDTO.getUserId()).updateTime(LocalDateTime.now()).build();

        orderDomainService.createOrder(order);

        return OrderCreateVO.builder().code("10000").build();

    }


}
