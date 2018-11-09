package no.ifi.uio.mediator.server.rest;

import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Controller
public class RabbitMQController {

    @Autowired
    private RabbitMQService rabbitMQService;

    @PostMapping("/post")
    public ResponseEntity postMessage(@RequestBody Message message) {
        try {
            rabbitMQService.postMessage(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/get")
    public ResponseEntity<Collection<GetResponse>> getMessages() {
        try {
            return ResponseEntity.ok(rabbitMQService.getMessages());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/ack/{deliveryTag}")
    public ResponseEntity ackMessage(@PathVariable long deliveryTag) {
        try {
            rabbitMQService.ackMessage(deliveryTag);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/ack")
    public ResponseEntity ackMessages(@RequestBody Collection<Long> deliveryTags) {
        try {
            rabbitMQService.ackMessages(deliveryTags);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
