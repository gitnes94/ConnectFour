package com.example.gameservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // needed for the @Scheduled outbox relay
public class GameserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GameserviceApplication.class, args);
    }
}
