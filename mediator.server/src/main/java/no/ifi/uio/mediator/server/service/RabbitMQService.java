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

@Slf4j
@Service
public class RabbitMQService {

    @Autowired
    private MessagePropertiesConverter messagePropertiesConverter;

    @Autowired
    private Channel channel;

    @Value("${queue}")
    private String queueName;

    public synchronized void postMessage(Message message) throws IOException {
        MessageProperties messageProperties = message.getMessageProperties();
        String exchange = messageProperties.getReceivedExchange();
        String routingKey = messageProperties.getReceivedRoutingKey();
        channel.basicPublish(exchange,
                routingKey,
                messagePropertiesConverter.fromMessageProperties(messageProperties, Charset.defaultCharset().toString()),
                message.getBody());
    }

    public synchronized Collection<GetResponse> getMessages() throws IOException {
        Collection<GetResponse> messages = new ArrayList<>();
        long messagesToRead = channel.messageCount(queueName);
        for (int i = 0; i < messagesToRead; i++) {
            GetResponse getResponse = channel.basicGet(queueName, false);
            messages.add(getResponse);
        }
        return messages;
    }

    public synchronized void ackMessage(long deliveryTag) throws IOException {
        channel.basicAck(deliveryTag, false);
    }

    public synchronized void ackMessages(Collection<Long> deliveryTags) {
        for (Long deliveryTag : deliveryTags) {
            try {
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
