package com.softuni.statssvc.scheduler;

import com.softuni.statssvc.service.JobStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsMaintenanceScheduler {

    private final JobStatService jobStatService;

    @Scheduled(cron = "0 0 3 * * SUN")
    public void purgeStaleStats() {
        log.info("Running weekly stale stats purge...");
        jobStatService.purgeStaleStats();
    }

    @Scheduled(fixedRate = 600000)
    public void logStatsSummary() {
        int count = jobStatService.findAll().size();
        log.info("Stats summary: {} category records currently tracked", count);
    }
}
