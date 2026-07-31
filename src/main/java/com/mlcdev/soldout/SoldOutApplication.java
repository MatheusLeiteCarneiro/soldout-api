package com.mlcdev.soldout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SoldOutApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoldOutApplication.class, args);
    }

}
