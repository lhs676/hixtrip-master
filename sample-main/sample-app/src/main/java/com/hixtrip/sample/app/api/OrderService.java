package com.hixtrip.sample.app.api;

import com.hixtrip.sample.client.order.dto.CommandOderCreateDTO;
import com.hixtrip.sample.client.order.dto.vo.OrderCreateVO;

/**
 * 订单的service层
 */
public interface OrderService {

    OrderCreateVO placeOrder(CommandOderCreateDTO commandOderCreateDTO);


}
