package com.hixtrip.sample.app.service;

import com.hixtrip.sample.app.api.PayService;
import com.hixtrip.sample.app.service.pay.PaymentStrategy;
import com.hixtrip.sample.client.order.dto.CommandPayDTO;
import com.hixtrip.sample.domain.pay.PayDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PayServiceImpl implements PayService {

    private final Map<String, PaymentStrategy> paymentStrategies;

    @Autowired
    public PayServiceImpl(List<PaymentStrategy> paymentStrategies) {
        this.paymentStrategies = paymentStrategies.stream()
                .collect(Collectors.toMap(PaymentStrategy::getStatus, Function.identity()));
    }

    @Autowired
    private PayDomainService payDomainService;

    @Override
    public void processPayment(CommandPayDTO commandPay) {
        String payStatus = commandPay.getPayStatus();
        PaymentStrategy strategy = paymentStrategies.get(payStatus);
        if (strategy != null) {
            //把订单ID保存到redis中防止重复执行
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            Boolean lock = payDomainService.repeatedConsumption(executor, commandPay.getOrderId());
            if (lock){
                strategy.processPayment(commandPay.getOrderId());
                executor.shutdown();
            }

        } else {
            // 处理未知状态的情况，可以抛出异常或者做其他处理
            System.out.println("未知支付状态：" + payStatus);
        }

    }

}
