package rodrigue.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListenerModerate {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQListenerModerate.class);

    public void receiveMessage(String message) {
        logger.info("Received message on moderate listener: {}", message);
    }

}
