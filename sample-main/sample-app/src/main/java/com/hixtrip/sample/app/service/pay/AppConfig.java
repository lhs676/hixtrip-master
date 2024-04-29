package com.hixtrip.sample.app.service.pay;

import com.hixtrip.sample.app.service.PayServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AppConfig {
    @Bean
    public PayServiceImpl paymentService(List<PaymentStrategy> paymentStrategies) {
        return new PayServiceImpl(paymentStrategies);
    }
}


