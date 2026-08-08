package com.softuni.statssvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StatsSvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatsSvcApplication.class, args);
    }
}
