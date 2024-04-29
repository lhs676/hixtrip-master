package com.hixtrip.sample.infra;

import com.hixtrip.sample.domain.pay.repository.PayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class PayRepositoryImpl implements PayRepository {

    private static final String PAY_ORDER_PFX = "PAY_ORDER_PFX:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean insertOrder(String orderId) {

        String key = PAY_ORDER_PFX + orderId;

        if (redisTemplate.opsForValue().get(key) != null) {
            return false;
        }

        redisTemplate.opsForValue().set(key, orderId, 10, TimeUnit.SECONDS);

        return true;
    }

    @Override
    public void ResetPayOrderTime(String orderId) {

        String key = PAY_ORDER_PFX + orderId;

        RedisScript<String> script = new DefaultRedisScript<>(
                "if redis.call('EXISTS', KEYS[1]) == 1 then\n" +
                        "    redis.call('DEL', KEYS[1])\n" +
                        "end\n" +
                        "redis.call('SET', KEYS[1], ARGV[1])",
                String.class);
        redisTemplate.execute(script, Collections.singletonList(key), orderId);

    }
}
