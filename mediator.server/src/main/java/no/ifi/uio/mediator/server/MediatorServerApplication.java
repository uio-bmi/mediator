package no.ifi.uio.mediator.server;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

    @Bean
    public Channel channel(@Autowired ConnectionFactory connectionFactory) {
        for (String routingKey : routingKeys) {
            Binding binding = BindingBuilder.bind(queue()).to(exchange()).with(routingKey).noargs();
            rabbitAdmin.declareBinding(binding);
        }
        Connection connection = connectionFactory.createConnection();
        return connection.createChannel(false);
    }

    @Bean
    public Exchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue queue() {
        return new Queue(queueName, true, false, false);
    }

    @Bean
    public MessagePropertiesConverter messagePropertiesConverter() {
        return new DefaultMessagePropertiesConverter();
    }

}
