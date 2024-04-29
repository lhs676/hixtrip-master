package com.hixtrip.sample.domain.pay.repository;


/**
 *
 */
public interface PayRepository {


    Boolean insertOrder(String orderId);


    void ResetPayOrderTime(String orderId);
}
