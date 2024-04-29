package com.hixtrip.sample.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hixtrip.sample.domain.order.model.Order;
import com.hixtrip.sample.domain.order.repository.OrderRepository;
import com.hixtrip.sample.infra.db.convertor.OrderDOConvertor;
import com.hixtrip.sample.infra.db.dataobject.OrderDO;
import com.hixtrip.sample.infra.db.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderRepositoryImpl implements OrderRepository {

    private static final String ORDER_PFX = "ORDER：";

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDOConvertor orderDOConvertor;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    @Transactional
    public void createOrder(Order order) {

        OrderDO orderDO = orderDOConvertor.domainToDo(order);

        orderMapper.insert(orderDO);
        Order orderDomin = orderDOConvertor.doToDomain(orderDO);
        String key = ORDER_PFX + orderDO.getOrderId();

        try {
            String json = objectMapper.writeValueAsString(orderDomin);
            redisTemplate.opsForValue().set(key, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public Order selectOrderByOrderId(String orderId) {
        String key = ORDER_PFX + orderId;

        Object result = redisTemplate.opsForValue().get(key);
        Order order = null;
        // 从缓存中查询
        if (result != null) {
            try {
                order = objectMapper.readValue((String) result, Order.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        // 从数据库查
        if (order == null) {
            OrderDO orderDO = orderMapper.selectByOrderId(orderId);
            if (order != null) {
                order = orderDOConvertor.doToDomain(orderDO);

                try {
                    // 再添加到缓存
                    String json = objectMapper.writeValueAsString(order);
                    redisTemplate.opsForValue().set(key, json);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
        return order;
    }

    @Override
    @Transactional
    public void updateOrder(Order order) {
        String key = ORDER_PFX + order.getId();
        // 清除缓存
        redisTemplate.delete(key);
        //更新数据库
        OrderDO orderDO = orderDOConvertor.domainToDo(order);
        int result = orderMapper.updateByOrderId(orderDO);

        if (result != 1){
            System.out.println("更新失败");
            throw new RuntimeException();
        }
        // 休眠1秒再次删除缓存
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 清除缓存
        redisTemplate.delete(key);
    }


}
