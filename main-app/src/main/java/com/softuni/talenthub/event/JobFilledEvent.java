package com.softuni.talenthub.event;

import com.softuni.talenthub.model.entity.JobPost;
import org.springframework.context.ApplicationEvent;

public class JobFilledEvent extends ApplicationEvent {

    private final JobPost jobPost;

    public JobFilledEvent(Object source, JobPost jobPost) {
        super(source);
        this.jobPost = jobPost;
    }

    public JobPost getJobPost() {
        return jobPost;
    }
}
