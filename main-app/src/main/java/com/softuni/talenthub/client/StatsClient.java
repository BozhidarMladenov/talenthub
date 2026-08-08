package com.softuni.talenthub.client;

import com.softuni.talenthub.model.dto.StatRecordRequest;
import com.softuni.talenthub.model.dto.StatResponse;
import com.softuni.talenthub.model.dto.StatResponseCollection;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "stats-svc", url = "${stats-svc.url}")
public interface StatsClient {

    @GetMapping("/api/stats")
    StatResponseCollection getAllStats();

    @PostMapping("/api/stats")
    StatResponse recordStat(@RequestBody StatRecordRequest request);

    @PutMapping("/api/stats/{category}")
    StatResponse updateStat(@PathVariable String category, @RequestBody StatRecordRequest request);

    @DeleteMapping("/api/stats/{category}")
    void deleteStat(@PathVariable String category);
}
