package rodrigue.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListener {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);

    public void receiveMessage(String message) {
        logger.info("Received message on default listener: {}", message);
    }

}
