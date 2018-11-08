package no.ifi.uio.mediator.server.rest;

import com.rabbitmq.client.GetResponse;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class RabbitMQController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/post/{exchange}/{routingKey}")
    public ResponseEntity postMessage(@PathVariable String exchange,
                                      @PathVariable String routingKey,
                                      @RequestBody Message message) {
        try {
            rabbitTemplate.send(exchange, routingKey, message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/get/{queue}")
    public ResponseEntity<GetResponse> getMessages(@PathVariable String queue) {
        try {
            return rabbitTemplate.execute(channel -> ResponseEntity.ok(channel.basicGet(queue, false)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ack/{deliveryTag}")
    public ResponseEntity ackMessage(@PathVariable long deliveryTag) {
        try {
            return rabbitTemplate.execute((ChannelCallback<ResponseEntity>) channel -> {
                channel.basicAck(deliveryTag, false);
                return ResponseEntity.status(HttpStatus.OK).build();
            });
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
