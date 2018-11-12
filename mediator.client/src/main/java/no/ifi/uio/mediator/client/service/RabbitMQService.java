package no.ifi.uio.mediator.client.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;

/**
 * Service for:
 * - forwarding messages from the Broker to the Server side;
 * - getting messages from Server and placing them to a Broker.
 */
@Slf4j
@Service
public class RabbitMQService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MessagePropertiesConverter messagePropertiesConverter;

    /**
     * Listens to queues, specified in properties, and sends received messages to the Server.
     * Acknowledges Broker after successful message forwarding.
     *
     * @param channel RabbitMQ channel.
     * @param message Received message.
     */
    @RabbitListener(queues = "#{'${queues}'.split(',')}")
    public void receiveMessage(Channel channel, Message message) {
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("http://mediator-server/post", message, Void.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Runs on schedule, requesting all new messages from the Server and putting them to the Broker.
     * Acknowledges Server after successful message forwarding.
     */
    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    public void dumpMessages() {
        ResponseEntity<Collection<GetResponse>> responseEntity = restTemplate.exchange("http://mediator-server/get",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Collection<GetResponse>>() {
                });
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.error(responseEntity.toString());
            return;
        }
        Collection<GetResponse> messages = responseEntity.getBody();
        if (messages == null || messages.size() == 0) {
            return;
        }
        log.info("Received {} messages.", messages.size());
        for (GetResponse message : messages) {
            Envelope envelope = message.getEnvelope();
            String exchange = envelope.getExchange();
            String routingKey = envelope.getRoutingKey();
            MessageProperties messageProperties = messagePropertiesConverter.toMessageProperties(message.getProps(), envelope, Charset.defaultCharset().toString());
            rabbitTemplate.send(exchange, routingKey, new Message(message.getBody(), messageProperties));
            restTemplate.postForEntity("http://mediator-server/ack/" + messageProperties.getDeliveryTag(), null, Void.class);
        }
    }

}
