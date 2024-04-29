package com.hixtrip.sample.app.service.pay;

public interface PaymentStrategy {
    void processPayment(String orderId);

    String getStatus();
}
