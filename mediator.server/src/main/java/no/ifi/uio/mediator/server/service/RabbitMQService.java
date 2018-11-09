package no.ifi.uio.mediator.server.service;

import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service
public class RabbitMQService {

    @Autowired
    private AmqpAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public synchronized void postMessage(Message message) {
        MessageProperties messageProperties = message.getMessageProperties();
        String exchange = messageProperties.getReceivedExchange();
        String routingKey = messageProperties.getReceivedRoutingKey();
        rabbitTemplate.send(exchange, routingKey, message);
    }

    public synchronized Collection<GetResponse> getMessages(String queue) {
        Object messageCount = rabbitAdmin.getQueueProperties(queue).get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
        int messagesToRead = messageCount == null ? 0 : (int) messageCount;
        return rabbitTemplate.execute(channel -> {
            Collection<GetResponse> messages = new ArrayList<>();
            for (int i = 0; i < messagesToRead; i++) {
                messages.add(channel.basicGet(queue, false));
            }
            return messages;
        });
    }

    public synchronized void ackMessage(long deliveryTag) {
        rabbitTemplate.execute((ChannelCallback<Void>) channel -> {
            channel.basicAck(deliveryTag, false);
            return null;
        });
    }

    public synchronized void ackMessages(Collection<Long> deliveryTags) {
        rabbitTemplate.execute((ChannelCallback<Void>) channel -> {
            for (Long deliveryTag : deliveryTags) {
                try {
                    channel.basicAck(deliveryTag, false);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            return null;
        });
    }

}
