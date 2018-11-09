package no.ifi.uio.mediator.client;

import com.google.gson.Gson;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

/**
 * Spring Boot application's main class with some configuration and some beans defined.
 */
@EnableScheduling
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, JacksonAutoConfiguration.class})
public class MediatorClientApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(MediatorClientApplication.class, args);
    }

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(new Gson());
        restTemplate.setMessageConverters(Collections.singletonList(gsonHttpMessageConverter));
        return restTemplate;
    }

    @Bean
    public MessagePropertiesConverter messagePropertiesConverter() {
        return new DefaultMessagePropertiesConverter();
    }

}
