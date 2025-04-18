package com.diogo.crypto.scheduler;

import com.diogo.crypto.services.PriceMonitorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceScheduler {
    private PriceMonitorService priceMonitorService;

    public PriceScheduler(PriceMonitorService priceMonitorService) {
        this.priceMonitorService = priceMonitorService;
    }

    @Scheduled(fixedRate = 10000)
    private void updatePrices() {
        priceMonitorService.checkPrice("BTCUSDT");
        priceMonitorService.checkPrice("SOLUSDT");
    }
}
