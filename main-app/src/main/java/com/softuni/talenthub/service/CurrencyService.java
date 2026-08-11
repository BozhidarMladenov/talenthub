package com.softuni.talenthub.service;

import com.softuni.talenthub.client.ExchangeRateClient;
import com.softuni.talenthub.model.dto.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {

    private static final String BASE = "USD";
    private static final Map<String, String> DISPLAY_CURRENCIES = new LinkedHashMap<>();

    static {
        DISPLAY_CURRENCIES.put("USD", "$");
        DISPLAY_CURRENCIES.put("EUR", "€");
        DISPLAY_CURRENCIES.put("GBP", "£");
        DISPLAY_CURRENCIES.put("BGN", "лв");
    }

    private final ExchangeRateClient exchangeRateClient;

    
    @Lazy
    @Autowired
    private CurrencyService self;

    @Cacheable("exchangeRates")
    public Map<String, BigDecimal> getLatestRates() {
        log.info("Fetching live exchange rates from open.exchangerate-api.com");
        try {
            ExchangeRateResponse response = exchangeRateClient.getLatestRates(BASE);
            return response.getRates();
        } catch (Exception e) {
            log.warn("Could not fetch exchange rates: {}. Returning USD only.", e.getMessage());
            Map<String, BigDecimal> fallback = new LinkedHashMap<>();
            fallback.put("USD", BigDecimal.ONE);
            return fallback;
        }
    }

    public Map<String, String> convertBudget(BigDecimal usdAmount) {
        Map<String, BigDecimal> rates = self.getLatestRates();
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : DISPLAY_CURRENCIES.entrySet()) {
            String code = entry.getKey();
            String symbol = entry.getValue();
            BigDecimal rate = rates.getOrDefault(code, BigDecimal.ONE);
            BigDecimal converted = usdAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            result.put(code, symbol + converted);
        }
        return result;
    }
}
