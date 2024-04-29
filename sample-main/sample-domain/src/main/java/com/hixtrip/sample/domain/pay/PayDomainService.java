package com.hixtrip.sample.domain.pay;

import com.hixtrip.sample.domain.pay.model.CommandPay;
import com.hixtrip.sample.domain.pay.repository.PayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 支付领域服务
 * todo 不需要具体实现, 直接调用即可
 */
@Component
public class PayDomainService {


    @Autowired
    private PayRepository payRepository;

    /**
     * 记录支付回调结果
     * 【高级要求】至少有一个功能点能体现充血模型的使用。
     */
    public void payRecord(CommandPay commandPay) {
        //无需实现，直接调用即可
    }

    /**
     * 把订单ID保存到redis中防止重复执行
     */
    public Boolean repeatedConsumption(ScheduledExecutorService executor,String orderId) {

        Boolean result = payRepository.insertOrder(orderId);
        // 另起一个线程延续这个key的时间
        if (result){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executor.schedule(()->{
                payRepository.ResetPayOrderTime(orderId);
            },8, TimeUnit.SECONDS);

        }
        return result;


    }
}
