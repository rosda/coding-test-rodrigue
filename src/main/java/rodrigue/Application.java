package rodrigue;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rodrigue.listeners.RabbitMQListener;
import rodrigue.listeners.RabbitMQListenerHigh;
import rodrigue.listeners.RabbitMQListenerModerate;

@RestController
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties
public class Application {

    @Value("${spring.application.name}")
    private String name;

    @Value("${spring.rabbitmq.template.exchange}")
    private String topicExchange;

    @Value("${spring.rabbitmq.queue.default}")
    private String defaultQueueName;

    @Value("${spring.rabbitmq.queue.moderate}")
    private String moderateQueueName;

    @Value("${spring.rabbitmq.queue.high}")
    private String highQueueName;

    /**
     * Bean declarations to instantiate queues, exchange, listeners and bind them together. We are following the described
     * scenario of high, moderate keywords use and starting up with 3 queues for the specific purpose.
     *
     * If it is required, we can inject dependencies on template creations to pick up queues inserted via the RabbitMQ
     * Manager admin interface.
     *
     * The beans instanciated keep the connection to RabbitMQ alive as long as the application is running, if an error
     * get propagated and a channel or connection is shut down, the service attempt to reopen it until x amount of trials.
     * While this is happening, the REST API will respond appropriately with a temporarily unavailable response (503)
     * until the connection is re-established. The connection and reponse timeouts are set by default to 70 milliseconds
     * in the property file. Any change needs to be done in the subsequent file or your own local property application
     * (which will override the file's values).
     *
     */

    @Bean
    Queue defaultQueue() {
        return new Queue(defaultQueueName, false);
    }

    @Bean
    Binding defaultQueueBinding(Queue defaultQueue, TopicExchange exchange) {
        return BindingBuilder.bind(defaultQueue).to(exchange).with(defaultQueueName);
    }

    @Bean
    MessageListenerAdapter defaultListenerAdapter(RabbitMQListener receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean
    SimpleMessageListenerContainer defaultContainer(ConnectionFactory connectionFactory, MessageListenerAdapter defaultListenerAdapter) {
        return container(connectionFactory, defaultListenerAdapter, defaultQueueName);
    }

    @Bean
    Queue moderateQueue() {
        return new Queue(moderateQueueName, false);
    }

    @Bean
    Binding moderateQueueBinding(Queue moderateQueue, TopicExchange exchange) {
        return BindingBuilder.bind(moderateQueue).to(exchange).with(moderateQueueName);
    }

    @Bean
    MessageListenerAdapter moderateListenerAdapter(RabbitMQListenerModerate receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean
    SimpleMessageListenerContainer moderateContainer(ConnectionFactory connectionFactory, MessageListenerAdapter moderateListenerAdapter) {
        return container(connectionFactory, moderateListenerAdapter, moderateQueueName);
    }

    @Bean
    Queue highQueue() {
        return new Queue(highQueueName, false);
    }

    @Bean
    Binding highQueueBinding(Queue highQueue, TopicExchange exchange) {
        return BindingBuilder.bind(highQueue).to(exchange).with(highQueueName);
    }

    @Bean
    MessageListenerAdapter highListenerAdapter(RabbitMQListenerHigh receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean
    SimpleMessageListenerContainer highContainer(ConnectionFactory connectionFactory, MessageListenerAdapter highListenerAdapter) {
        return container(connectionFactory, highListenerAdapter, highQueueName);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(topicExchange);
    }

    @RequestMapping("/")
    String index() {
        return name + " Running!";
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(Application.class, args);
    }

    /**
     * Container for RabbitMQ template configurations.
     *
     * @param connectionFactory connection
     * @param highListenerAdapter message adapter
     * @param queue queue name
     *
     * @return SimpleMessageListenerContainer
     */
    private SimpleMessageListenerContainer container(
            ConnectionFactory connectionFactory,
            MessageListenerAdapter highListenerAdapter,
            String queue
    ) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queue);
        container.setMessageListener(highListenerAdapter);

        return container;
    }

}