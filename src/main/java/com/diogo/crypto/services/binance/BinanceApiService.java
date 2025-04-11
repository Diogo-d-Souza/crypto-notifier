package com.diogo.crypto.services.binance;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class BinanceApiService {
    private final String BINANCE_API_URL = "https://api.binance.com/api/v3/ticker/price?symbol=";
    private final RestTemplate restTemplate = new RestTemplate();

    public double getCurrentBTCPrice(String symbol) {
        ResponseEntity<Map> response = restTemplate.getForEntity(BINANCE_API_URL + symbol, Map.class);
        return Double.parseDouble(response.getBody().get("price").toString());
    }
}
