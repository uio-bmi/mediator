package no.ifi.uio.mediator.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * Spring Boot application's main class with some configuration and some beans defined.
 */
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class MediatorServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediatorServerApplication.class, args);
    }

}
