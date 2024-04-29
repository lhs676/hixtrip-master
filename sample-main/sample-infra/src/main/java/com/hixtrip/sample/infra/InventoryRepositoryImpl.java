package com.hixtrip.sample.infra;

import com.hixtrip.sample.domain.inventory.repository.InventoryRepository;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisScriptingCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * infra层是domain定义的接口具体的实现
 */
@Component
public class InventoryRepositoryImpl implements InventoryRepository {

    // 实际库存
    private static final String ACTUAL_INVENTORY_PREFIX = "ACTUAL_INVENTORY:";

    // 预留库存
    private static final String RESERVED_INVENTORY_PREFIX = "RESERVED_INVENTORY:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public Integer getInventoryBySkuId(String skuId) {
        // 可售内存 应该是 实际库存-预留库存
        String actualKey = ACTUAL_INVENTORY_PREFIX + skuId;

        Object actualValue = redisTemplate.opsForValue().get(actualKey);
        if (actualValue == null) {
            return 0;
        }
        String reservedKey = RESERVED_INVENTORY_PREFIX + skuId;

        Object reservedValue = redisTemplate.opsForValue().get(reservedKey);
        if (reservedValue == null) {
            return (Integer) actualValue;
        }

        return (Integer) actualValue - (Integer) reservedValue;
    }

    public int addReservedInventory(String skuId, Long quantity) {
        String luaScript = "local reservedKey = KEYS[1]  -- 预留库存键\n" +
                "local inventoryKey = KEYS[2]  -- 实际库存键\n" +
                "local reservedQuantity = tonumber(ARGV[1])  -- 新增预留库存数量\n" +
                "\n" +
                "local currentInventory = tonumber(redis.call('GET', inventoryKey))\n" +
                "local currentReserved = tonumber(redis.call('GET', reservedKey))\n" +
                "\n" +
                "if currentInventory == nil then\n" +
                "    currentInventory = 0\n" +
                "end\n" +
                "if currentReserved == nil then\n" +
                "    currentReserved = 0\n" +
                "end\n" +
                "\n" +
                "local totalReserved = currentReserved + reservedQuantity\n" +
                "\n" +
                "if currentInventory >= totalReserved then\n" +
                "    redis.call('INCRBY', reservedKey, reservedQuantity)\n" +
                "    return 1  -- 预留库存新增成功\n" +
                "else\n" +
                "    return 0 --0 实际库存不足\n" +
                "end\n";

        String actualKey = ACTUAL_INVENTORY_PREFIX + skuId;

        String reservedKey = RESERVED_INVENTORY_PREFIX + skuId;

        List<String> keys = Arrays.asList(reservedKey, actualKey);  // 传递多个键

        Object result = executeLuaScript(luaScript, keys, quantity);

        return result == null ? 0 : 1;
    }


    public int reduceReservedInventory(String skuId, Long quantity) {

        try {
            // 下单失败 个人认为不需要考虑原子性
            redisTemplate.opsForValue().decrement(RESERVED_INVENTORY_PREFIX + skuId, quantity);
        } catch (Exception e) {
//            e.printStackTrace();
            return 0;
        }

        return 1;
    }


    @Override
    public int reduceActualInventory(String skuId, Long quantity) {

        String luaScript = "local reservedKey = KEYS[1]  -- 预留库存键\n" +
                "local inventoryKey = KEYS[2]  -- 实际库存键\n" +
                "local quantity = tonumber(ARGV[1])  -- 扣除数量\n" +
                "\n" +
                "local currentReserved = tonumber(redis.call('GET', reservedKey))\n" +
                "local currentInventory = tonumber(redis.call('GET', inventoryKey))\n" +
                "\n" +
                "if currentReserved == nil then\n" +
                "    currentReserved = 0\n" +
                "end\n" +
                "if currentInventory == nil then\n" +
                "    currentInventory = 0\n" +
                "end\n" +
                "\n" +
                "if currentInventory >= quantity then\n" +
                "    redis.call('DECRBY', reservedKey, quantity)\n" +
                "    redis.call('DECRBY', inventoryKey, quantity)\n" +
                "    return 1  -- 扣除成功\n" +
                "else\n" +
                "    return 0  -- 实际库存不足\n" +
                "end\n";


        String reservedKey = RESERVED_INVENTORY_PREFIX + skuId;

        String actualKey = ACTUAL_INVENTORY_PREFIX + skuId;

        List<String> keys = Arrays.asList(reservedKey, actualKey);  // 传递多个键

        Object result = executeLuaScript(luaScript, keys, quantity);

        return result == null ? 0 : 1;


    }


    private Object executeLuaScript(String luaScript, List<String> keys, Object... args) {

        RedisScript<Object> redisScript = new DefaultRedisScript<>(
                luaScript, Object.class);
        Object result = redisTemplate.execute(redisScript, keys, args);

        return result;
    }


}
