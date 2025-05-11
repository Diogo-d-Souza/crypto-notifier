package com.diogo.crypto.services.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisMessagePublisher {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);
    private final StringRedisTemplate stringRedisTemplate;
    private static final String CHANNEL_NAME = "crypto-updates";

    public RedisMessagePublisher(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void publish(String message) {
        try {
            stringRedisTemplate.convertAndSend(CHANNEL_NAME, message);
            logger.info("Published message to Redis: {}", message);
        } catch (Exception e) {
            logger.error("Error publishing message to Redis: {}", e.getMessage());
        }
    }
}
