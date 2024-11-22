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

    private static final String LOCK_KEY = "process_locks";
    private static final int MAX_LOCK_COUNT = 2;
    private static final long LOCK_EXPIRE_TIME = 1;

    public boolean acquireLock() {
        Long expire = redisTemplate.getExpire(LOCK_KEY);
        if (expire > 0 && redisTemplate.opsForValue().get(LOCK_KEY).equals("1")) {
            System.out.println(expire);
            redisTemplate.opsForValue().set(LOCK_KEY, "2", expire, TimeUnit.SECONDS);
            return true;
        } else {
            return redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "1", LOCK_EXPIRE_TIME, TimeUnit.SECONDS);
        }

    }

}
