package com.softuni.talenthub.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StatsCacheEvictScheduler {

    @Scheduled(fixedDelay = 300000)
    @CacheEvict(value = {"allStats", "exchangeRates"}, allEntries = true)
    public void evictStatsCache() {
        log.info("Stats and exchange rate caches evicted — next request will fetch fresh data");
    }
}
