package com.diogo.crypto.services;

import com.diogo.crypto.entities.models.CryptoPrice;
import com.diogo.crypto.services.binance.BinanceApiService;
import com.diogo.crypto.services.redis.RedisMessagePublisher;
import com.diogo.crypto.services.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PriceMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(PriceMonitorService.class);
    private BinanceApiService binanceApiService;
    private MongoTemplate mongoTemplate;
    private RedisMessagePublisher redisMessagePublisher;
    private RedisService redisService;

    public PriceMonitorService(BinanceApiService binanceApiService, MongoTemplate mongoTemplate, RedisMessagePublisher redisMessagePublisher, RedisService redisService) {
        this.binanceApiService = binanceApiService;
        this.mongoTemplate = mongoTemplate;
        this.redisMessagePublisher = redisMessagePublisher;
        this.redisService = redisService;
    }

    public void checkPrice(String symbol) {
        logger.info("Checking price for symbol: {}", symbol);

        double currentCryptoCoinPrice = binanceApiService.getCurrentBTCPrice(symbol);
        double lastStoredPrice = redisService.getLastStoredPrice(symbol);

        logger.info("Last stored price: {}, current price: {}", lastStoredPrice, currentCryptoCoinPrice);

        String cryptoCoin = symbol.substring(0, symbol.length() - 4);

        if (lastStoredPrice > 0 && currentCryptoCoinPrice >= lastStoredPrice * 1.01) {
            String message = cryptoCoin + " price increased by 1%! New price: $" + currentCryptoCoinPrice;
            logger.warn("Price increased more than 1%! Sending notification.");
            logger.info("Publishing the message: {}", message);
            redisMessagePublisher.publish(message);
        }
        redisService.cacheCurrentPrice(symbol, currentCryptoCoinPrice);
        redisService.addPriceToHistory(symbol, currentCryptoCoinPrice);

        storePrice(symbol, currentCryptoCoinPrice);
    }
    
    private void storePrice(String symbol, double price) {
        mongoTemplate.save(new CryptoPrice(symbol, price, Instant.now()));

        Query query = new Query(Criteria.where("symbol").is(symbol));
        long count = mongoTemplate.count(query, CryptoPrice.class);

        if (count > 100) {
            Query deleteQuery = new Query(Criteria.where("symbol").is(symbol))
                    .with(Sort.by(Sort.Direction.ASC, "timestamp"))
                    .limit((int) (count - 100));

            mongoTemplate.remove(deleteQuery, CryptoPrice.class);
        }
    }
}
