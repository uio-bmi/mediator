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

/**
 * Controller for:
 * - posting message received from the Client to the Broker;
 * - getting all messages from the Broker and sending them to the Client (upon request);
 * - acknowledging messages receiving on the Client's side.
 */
@Slf4j
@Controller
public class RabbitMQController {

    @Autowired
    private RabbitMQService rabbitMQService;

    /**
     * Publishes message to the Broker (as is).
     *
     * @param message Message, received from the Client.
     * @return Empty ResponseEntity with the corresponding status code.
     */
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

    /**
     * Gets and returns all messages that are ready from the Broker.
     *
     * @return Collection of ResponseEntities with GetResponse messages.
     */
    @GetMapping("/get")
    public ResponseEntity<Collection<GetResponse>> getMessages() {
        try {
            return ResponseEntity.ok(rabbitMQService.getMessages());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Acknowledges single message by its delivery tag.
     *
     * @param deliveryTag Delivery tag of a message.
     * @return Empty ResponseEntity with the corresponding status code.
     */
    @PostMapping("/ack/{deliveryTag}")
    public ResponseEntity ackMessage(@PathVariable long deliveryTag) {
        try {
            rabbitMQService.ackMessage(deliveryTag);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Acknowledges multiple messages at once by their delivery tags.
     *
     * @param deliveryTags Delivery tags to use for acknowledging.
     * @return Empty ResponseEntity with the corresponding status code.
     */
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
