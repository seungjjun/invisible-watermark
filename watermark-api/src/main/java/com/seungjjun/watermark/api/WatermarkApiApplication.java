package com.seungjjun.watermark.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.seungjjun.watermark.api",
    "com.seungjjun.watermark.service"
})
public class WatermarkApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WatermarkApiApplication.class, args);
    }
}