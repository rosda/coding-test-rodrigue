package rodrigue.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rodrigue.dto.MessageRequestDTO;
import rodrigue.dto.MessagePOJO;
import rodrigue.handlers.exceptions.RabbitMQException;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class RabbitMQServiceImpl implements RabbitMQService {

    @Value("${spring.rabbitmq.queue.default}")
    private String defaultQueueName;

    @Value("${spring.rabbitmq.queue.moderate}")
    private String moderateQueueName;

    @Value("${spring.rabbitmq.queue.high}")
    private String highQueueName;

    @Value("${spring.rabbitmq.file.location.high}")
    private String highUseFile;

    @Value("${spring.rabbitmq.file.location.moderate}")
    private String moderateUseFile;

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQServiceImpl.class);

    private RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendMessage(MessageRequestDTO message) throws RabbitMQException, IOException {
        String queue = getQueueName(message.getPayload());

        logger.info("Sending payload to queue ({})", queue);

        try {
            rabbitTemplate.convertAndSend(queue, message.getPayload());
        } catch (Exception exception) {
            throw new RabbitMQException(exception.getMessage());
        }
    }

    private String getQueueName(String message) throws IOException {
        logger.info("Getting queue name");

        int moderateUseCount = 0, highUseCount = 0;

        ObjectMapper mapper = new ObjectMapper();

        MessagePOJO messagePOJO = mapper.readValue(message, MessagePOJO.class);

        String[] words = messagePOJO.getMessage().split(" ");

        List highUse = mapper.readValue(new File(highUseFile), List.class);
        List moderateUse = mapper.readValue(new File(moderateUseFile), List.class);

        for (String word : words) {
            for (Object highUseKeyword : highUse) {
                if (word.equalsIgnoreCase(highUseKeyword.toString())) {
                    highUseCount++;
                }
            }

            for (Object moderateUseKeyword : moderateUse) {
                if (word.equalsIgnoreCase(moderateUseKeyword.toString())) {
                    moderateUseCount++;
                }
            }
        }

        if (highUseCount > 0) {
            return highQueueName;
        } else if (moderateUseCount > 0) {
            return moderateQueueName;
        }

        return defaultQueueName;
    }
}
