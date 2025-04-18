package com.diogo.crypto.services.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisMessagePublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String CHANNEL_NAME = "crypto-updates";

    public RedisMessagePublisher(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void publish(String message) {
        stringRedisTemplate.convertAndSend(CHANNEL_NAME, message);
    }
}
