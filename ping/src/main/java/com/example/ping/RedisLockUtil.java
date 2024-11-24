package com.example.ping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisLockUtil {


    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisLockUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String KEY = "request";
    private static final int MAX_LOCK_COUNT = 2;
    private static final long LIMIT_TIME = 1000;


    public boolean isAllowed() {
        long now = System.currentTimeMillis();
        long windowStart = now - LIMIT_TIME;

        // 删除时间窗口外的所有请求记录
        redisTemplate.opsForZSet().removeRangeByScore(KEY, 0, windowStart);

        // 获取当前时间窗口内的请求数
        Long count = redisTemplate.opsForZSet().zCard(KEY);

        if (count != null && count >= MAX_LOCK_COUNT) {
            // 超出限制
            return false;
        }

        // 添加当前请求的时间戳
        redisTemplate.opsForZSet().add(KEY, String.valueOf(now), now);

        // 设置过期时间（防止数据长期占用内存）
        redisTemplate.expire(KEY, LIMIT_TIME, TimeUnit.MILLISECONDS);
        return true;
    }
}
