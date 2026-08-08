package com.softuni.talenthub.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StatsCacheEvictScheduler {

    @Scheduled(fixedDelay = 300000)
    @CacheEvict(value = "allStats", allEntries = true)
    public void evictStatsCache() {
        log.info("Stats cache evicted — next request will fetch fresh data from stats-svc");
    }
}
