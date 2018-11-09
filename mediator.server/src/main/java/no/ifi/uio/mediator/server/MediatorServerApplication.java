package no.ifi.uio.mediator.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Boot application's main class with some configuration and some beans defined.
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, JacksonAutoConfiguration.class})
public class MediatorServerApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(MediatorServerApplication.class, args);
    }

}
