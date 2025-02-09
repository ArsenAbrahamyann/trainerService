package org.example.trainer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class TrainerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainerServiceApplication.class, args);
    }

}
