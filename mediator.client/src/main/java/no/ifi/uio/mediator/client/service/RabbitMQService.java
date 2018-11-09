package no.ifi.uio.mediator.client.service;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Service
public class RabbitMQService {

    @Autowired
    private RestTemplate restTemplate;

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

}
