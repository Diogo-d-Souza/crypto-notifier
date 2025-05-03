package com.diogo.crypto.services.redis;

import com.diogo.crypto.entities.models.CryptoPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);
    private final StringRedisTemplate redisTemplate;
    private MongoTemplate mongoTemplate;

    public RedisService(StringRedisTemplate redisTemplate, MongoTemplate mongoTemplate) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    public void cacheCurrentPrice(String symbol, Double price) {
        logger.debug("Cached current price in Redis for {}: {}", symbol, price);
        String key = "price:" + symbol;
        redisTemplate.opsForValue().set(key, String.valueOf(price));
    }

    public void addPriceToHistory(String symbol, Double price) {
        String key = "history:" + symbol;
        redisTemplate.opsForList().leftPush(key, String.valueOf(price));
        redisTemplate.opsForList().trim(key, 0, 9);
    }

    public double getLastStoredPrice(String symbol) {
        String redisKey = "price:" + symbol;
        String cachedPrice = redisTemplate.opsForValue().get(redisKey);
        logger.debug("Fetched last price from Redis for {}: {}", symbol, cachedPrice);

        if (cachedPrice != null) {
            return Double.parseDouble(cachedPrice);
        }
        logger.warn("No cached price found in Redis for {}", symbol);

        Query query = new Query(Criteria.where("symbol").is(symbol));
        query.with(Sort.by(Sort.Direction.DESC, "timestamp")).limit(1);
        CryptoPrice lastEntry = mongoTemplate.findOne(query, CryptoPrice.class);
        return lastEntry != null ? lastEntry.getPrice() : 0;
    }
}
