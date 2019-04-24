package rodrigue.services;

import rodrigue.dto.MessageRequestDTO;
import rodrigue.handlers.exceptions.RabbitMQException;

import java.io.IOException;

public interface RabbitMQService {

    /**
     * Send a message to the RabbitMQ queue.
     * @param message The payload message to send
     */
    void sendMessage(MessageRequestDTO message) throws RabbitMQException, IOException;
}
