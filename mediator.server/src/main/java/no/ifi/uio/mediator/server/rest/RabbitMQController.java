package no.ifi.uio.mediator.server.rest;

import com.rabbitmq.client.GetResponse;
import no.ifi.uio.mediator.server.service.RabbitMQService;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;

@Controller
public class RabbitMQController {

    @Autowired
    private RabbitMQService rabbitMQService;

    @PostMapping("/post/{exchange}/{routingKey}")
    public ResponseEntity postMessage(@PathVariable String exchange,
                                      @PathVariable String routingKey,
                                      @RequestBody Message message) {
        try {
            rabbitMQService.postMessage(exchange, routingKey, message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/get/{queue}")
    public ResponseEntity<Collection<GetResponse>> getMessages(@PathVariable String queue) {
        try {
            return ResponseEntity.ok(rabbitMQService.getMessages(queue));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/ack")
    public ResponseEntity ackMessages(@RequestBody Collection<Long> deliveryTags) {
        try {
            rabbitMQService.ackMessages(deliveryTags);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
