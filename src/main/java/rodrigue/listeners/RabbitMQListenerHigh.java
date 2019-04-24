package rodrigue.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListenerHigh {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQListenerHigh.class);

    public void receiveMessage(String message) {
        logger.info("Received message on high-use listener: {}", message);
    }

}
