# Rodrigue Simple Messaging Application using RabbitMQ and Spring Boot

## Short Description

This is a simple Spring Boot Application that can be run as a service, this small implementation
 demonstrate the ability to permanently connect to a RabbitMQ server.
  
The application exposes an endpoint to accept incoming messages and forward them onto specific RabbitMQ
 queues.
 
The service has ideally 3 features:
- Receive a message via an endpoint
- Classify the message received
- Place the message received in a matching queue without having to create a new connection to the RabbitMQ server

For this scenario, we have decided to construct a pattern analyzer to deconstruct messages and classify them
as highly, moderately used. The application highlights keywords or phrases used frequently and places them in the 
specific queue, this can be used for targeted advertising, conversational behaviour (user does not entertain long conversations)
or possibly for security and forensic (words are checked against datasource and classified as _suspicious, etc._).

## Service Integration

The critical library dependency of this service is primarily RabbitMQ for pub/sub interaction. For the purpose of this exercise 
We have used Elasticsearch as the analyzer (although we would prefer running it externally).

Initially the service classify the message by analyzing the words against a sample json file located in the resource folder 
and rank them accordingly, if elasticsearch is enabled, we insert the payload that will be later picked up by a background
process to fire up the analyzer, rank the words and write them back to the file. As the analysis is a process that takes time,
it cannot be run synchronously with the API Requests.

### Service Info and Dependencies

- Spring Boot (v2.0.5.RELEASE)
- Java (12.0.1) 
- Gradle (5.4)
- Linux Ubuntu 18.04
- RabbitMQ (3.6.15)
- Elasticsearch (6.6.0)

The IDE of use was JetBrains IntelliJ Idea.

### Configurations

To not extend this user guide, we are only highlighting a few properties. Please note that all properties can
be edited to your liking.

````
server.port = 8970
logging.file = ./logs/application.log
logging.level.org.springframework.web = ERROR
logging.level.root = INFO
spring.rabbitmq.host = localhost
spring.rabbitmq.port = 5672
spring.rabbitmq.username = guest
spring.rabbitmq.password = guest
spring.rabbitmq.queue.default = keywords_other
spring.rabbitmq.template.exchange = rodrigue_coding_test_exchange
spring.rabbitmq.listener.simple.concurrency=8
spring.rabbitmq.listener.simple.max-concurrency=16
spring.rabbitmq.template.receive-timeout = 70
spring.rabbitmq.connection-timeout = 70
spring.rabbitmq.template.reply-timeout = 70
spring.rabbitmq.cache.channel.checkout-timeout = 70
spring.rabbitmq.queue.moderate = keywords_moderate
spring.rabbitmq.file.location.moderate = ./src/main/resources/default.moderate-use.json
spring.rabbitmq.queue.high = keywords_high
spring.rabbitmq.file.location.high = ./src/main/resources/default.high-use.json
spring.elasticsearch.clustername = elasticsearch_rodrigue_cluster
spring.elasticsearch.host = localhost
spring.elasticsearch.port = 9300
spring.elasticsearch.words.limit = 1000
````

### Run (Interactive)

To run this service interactively just go to the root of the project and type:
`java -jar rodmsg` or if you have a configuration file located somewhere else (accessible to the service) you can
also type: `java -jar rodmsg --spring.config.location=file:path_to_config_file`

Alternatively, you could also build it from scratch:
````
./gradlew build
./gradlew bootRun
````

### Run (Service) (Daemon)

In order to run this service as a daemon, you'll need to copy the file _rodmsg.service_ to your systemd folder
and follow the steps below:

`sudo cp rodmsg.service /etc/systemd/system/`

open the file and replace the appropriate values.

```
[Unit]
Description=Rodrigue Messaging Service
After=syslog.target

[Service]
User=rodrigue
ExecStart=/home/rodrigue/apps/private/spring-messaging-pattern/rodmsg
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

Replace the **User** with the user that will be running the service and **ExecStart** value by the path where 
you have located your file.

Once the service file has been created, you need reload the daemon service to pick up the
new changes and enable the newly added service via the below command. 

`sudo systemctl daemon-reload`

`sudo systemctl enable rodmsg.service`

This will give you the ability to use the service commands to fire the
**start/stop/restart/status** events.

`sudo service rodmsg start|stop|status`
 or
 `sudo systemctl start|stop|status rodmsg.service`
 
If the service has started successfully, it will expose an endpoint located at
 <a href="http://localhost:8970/">http://localhost:8970/</a>. If you have changed the port,
it will expose the service at the specified port.

The service is now ready to be consumed. The API path is `/api/v1/message`.