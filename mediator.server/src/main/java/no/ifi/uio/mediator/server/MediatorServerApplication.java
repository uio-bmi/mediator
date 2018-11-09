package no.ifi.uio.mediator.server;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;

/**
 * Spring Boot application's main class with some configuration and some beans defined.
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, JacksonAutoConfiguration.class})
public class MediatorServerApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(MediatorServerApplication.class, args);
    }

    @Autowired
    private AmqpAdmin rabbitAdmin;

    @Value("${queue}")
    private String queueName;

    @Value("${exchange}")
    private String exchange;

    @Value("${keys}")
    private String[] routingKeys;

    @PostConstruct
    public void init() {
        for (String routingKey : routingKeys) {
            Binding binding = BindingBuilder.bind(queue()).to(exchange()).with(routingKey).noargs();
            rabbitAdmin.declareBinding(binding);
        }
    }

    @Bean
    public Exchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue queue() {
        return new Queue(queueName, true, false, false);
    }

}
