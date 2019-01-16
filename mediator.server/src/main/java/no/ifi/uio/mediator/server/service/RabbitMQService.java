package no.ifi.uio.mediator.server.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Service for:
 * - posting message received from the Client to the Broker;
 * - getting all messages from the Broker and sending them to the Client (upon request);
 * - acknowledging messages receiving on the Client's side.
 */
@Slf4j
@Service
public class RabbitMQService {

    @Autowired
    private MessagePropertiesConverter messagePropertiesConverter;

    @Autowired
    private Channel channel;

    @Value("${queue}")
    private String queueName;

    /**
     * Publishes message to the Broker (as is).
     *
     * @param message Message, received from the Client.
     * @throws IOException In case of error during publishing.
     */
    public synchronized void postMessage(Message message) throws IOException {
        MessageProperties messageProperties = message.getMessageProperties();
        String exchange = messageProperties.getReceivedExchange();
        String routingKey = messageProperties.getReceivedRoutingKey();
        log.info("Posting to exchange {}, with routing key {}", exchange, routingKey);
        channel.basicPublish(exchange,
                routingKey,
                messagePropertiesConverter.fromMessageProperties(messageProperties,
                        Charset.defaultCharset().toString()),
                message.getBody());
    }

    /**
     * Gets and returns all messages that are ready from the Broker.
     *
     * @return Collection of messages as GetResponse entities.
     * @throws IOException In case of error during messages retrieving.
     */
    public synchronized Collection<GetResponse> getMessages() throws IOException {
        Collection<GetResponse> messages = new ArrayList<>();
        long messagesToRead = channel.messageCount(queueName);
        if (messagesToRead != 0) {
            log.info("Returning {} messages to the Client.", messagesToRead);
        }
        for (int i = 0; i < messagesToRead; i++) {
            GetResponse getResponse = channel.basicGet(queueName, false);
            messages.add(getResponse);
        }
        return messages;
    }

    /**
     * Acknowledges single message by its delivery tag.
     *
     * @param deliveryTag Delivery tag of a message.
     * @throws IOException In case of error during message acknowledging.
     */
    public synchronized void ackMessage(long deliveryTag) throws IOException {
        log.info("Acknowledging {}", deliveryTag);
        channel.basicAck(deliveryTag, false);
    }

    /**
     * Acknowledges multiple messages at once by their delivery tags.
     *
     * @param deliveryTags Delivery tags to use for acknowledging.
     */
    public synchronized void ackMessages(Collection<Long> deliveryTags) {
        for (Long deliveryTag : deliveryTags) {
            try {
                ackMessage(deliveryTag);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
