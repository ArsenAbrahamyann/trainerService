package org.example.trainer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableDiscoveryClient
@EnableRetry
@EnableJms
public class TrainerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainerServiceApplication.class, args);
    }

}
