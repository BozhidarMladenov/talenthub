package com.softuni.talenthub.service;

import com.softuni.talenthub.client.StatsClient;
import com.softuni.talenthub.model.dto.StatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsClient statsClient;

    @Cacheable("allStats")
    public List<StatResponse> getAllStats() {
        log.info("Fetching all stats from stats-svc");
        try {
            return statsClient.getAllStats().getContent();
        } catch (Exception e) {
            log.warn("Stats service unavailable: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
