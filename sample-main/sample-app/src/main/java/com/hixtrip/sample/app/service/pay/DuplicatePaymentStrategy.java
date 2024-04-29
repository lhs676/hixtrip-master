package com.hixtrip.sample.app.service.pay;

public class DuplicatePaymentStrategy implements PaymentStrategy {
    private static final  String status = "DUPLICATE";

    @Override
    public void processPayment(String orderId) {

            // 处理重复支付逻辑


    }

    @Override
    public String getStatus() {
        return status;
    }
}