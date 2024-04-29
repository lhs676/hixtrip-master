package com.hixtrip.sample.domain.order.repository;

import com.hixtrip.sample.domain.order.model.Order;

/**
 *
 */
public interface OrderRepository {

    void createOrder(Order order);

    Order selectOrderByOrderId(String orderId);

    void updateOrder(Order order);
}
