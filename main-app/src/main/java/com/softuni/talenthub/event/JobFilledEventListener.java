package com.softuni.talenthub.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobFilledEventListener {

    @Async
    @EventListener
    public void onJobFilled(JobFilledEvent event) {
        log.info("[EVENT] Job '{}' (id={}, category={}) has been marked as FILLED by client '{}'.",
                event.getJobPost().getTitle(),
                event.getJobPost().getId(),
                event.getJobPost().getCategory(),
                event.getJobPost().getClient().getUsername());
    }
}
