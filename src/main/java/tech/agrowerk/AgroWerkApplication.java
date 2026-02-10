package tech.agrowerk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgroWerkApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgroWerkApplication.class, args);
    }

}
