package com.diogo.crypto.services.binance;

import com.diogo.crypto.services.PriceMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class BinanceApiService {
    private static final Logger logger = LoggerFactory.getLogger(PriceMonitorService.class);
    private final String BINANCE_API_URL = "https://api.binance.com/api/v3/ticker/price?symbol=";
    private final RestTemplate restTemplate = new RestTemplate();

    public double getCurrentBTCPrice(String symbol) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(BINANCE_API_URL + symbol, Map.class);
            logger.debug("Getting the price for symbol: {}", symbol);
            return Double.parseDouble(response.getBody().get("price").toString());
        } catch (Exception e) {
            logger.error("Error fetching price from Binance for symbol {}: {}", symbol, e.getMessage());
            return -1;
        }
    }
}
