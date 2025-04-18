package com.diogo.crypto.services;

import com.diogo.crypto.entities.models.CryptoPrice;
import com.diogo.crypto.services.binance.BinanceApiService;
import com.diogo.crypto.services.redis.RedisMessagePublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PriceMonitorService {
    private BinanceApiService binanceApiService;
    private MongoTemplate mongoTemplate;
    private RedisMessagePublisher redisMessagePublisher;

    public PriceMonitorService(BinanceApiService binanceApiService, MongoTemplate mongoTemplate, RedisMessagePublisher redisMessagePublisher) {
        this.binanceApiService = binanceApiService;
        this.mongoTemplate = mongoTemplate;
        this.redisMessagePublisher = redisMessagePublisher;
    }

    public void checkPrice(String symbol) {
        double currentCryptoCoinPrice = binanceApiService.getCurrentBTCPrice(symbol);
        double lastStoredPrice = getLastStoredPrice(symbol);
        String cryptoCoin = symbol.substring(0, symbol.length() - 4);

        if (lastStoredPrice > 0 && currentCryptoCoinPrice >= lastStoredPrice * 1.01) {
            String message = cryptoCoin + " price increased by 1%! New price: $" + currentCryptoCoinPrice;
            redisMessagePublisher.publish(message);
        }

        storePrice(symbol, currentCryptoCoinPrice);
    }

    private double getLastStoredPrice(String symbol) {
        Query query = new Query(Criteria.where("symbol").is(symbol));
        query.with(Sort.by(Sort.Direction.DESC, "timestamp")).limit(1);
        CryptoPrice lastEntry = mongoTemplate.findOne(query, CryptoPrice.class);
        return lastEntry != null ? lastEntry.getPrice() : 0;
    }

    private void storePrice(String symbol, double price) {
        Query query = new Query(Criteria.where("symbol").is(symbol));
        query.with(Sort.by(Sort.Direction.ASC, "timestamp"));

        List<CryptoPrice> prices = mongoTemplate.find(query, CryptoPrice.class);
        if (prices.size() >= 10) {
            mongoTemplate.remove(prices.get(0));
        }

        mongoTemplate.save(new CryptoPrice(symbol, price, Instant.now()));
    }
}
