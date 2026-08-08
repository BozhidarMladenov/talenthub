package com.softuni.talenthub.client;

import com.softuni.talenthub.model.dto.ExchangeRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "exchange-rate-client", url = "https://open.exchangerate-api.com/v6")
public interface ExchangeRateClient {

    @GetMapping("/latest/{base}")
    ExchangeRateResponse getLatestRates(@PathVariable String base);
}
