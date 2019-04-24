package rodrigue.controller;

import rodrigue.dto.MessageRequestDTO;
import rodrigue.dto.MessageResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import rodrigue.handlers.exceptions.RabbitMQException;
import rodrigue.services.ElasticService;
import rodrigue.services.RabbitMQService;

import javax.validation.Valid;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(value = "/api", produces = APPLICATION_JSON_VALUE)
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    private ElasticService elasticService;

    private RabbitMQService rabbitMQService;

    @Autowired
    public MessageController(ElasticService elasticService, RabbitMQService rabbitMQService) {
        this.elasticService = elasticService;
        this.rabbitMQService = rabbitMQService;
    }

    @RequestMapping(value = "/v1/message", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponseDTO> message(
            @Valid
            @RequestBody MessageRequestDTO messageRequestDTO
    ) throws RabbitMQException, IOException {

        rabbitMQService.sendMessage(messageRequestDTO);

        elasticService.saveIndex(messageRequestDTO);

        return new ResponseEntity<>(new MessageResponseDTO(HttpStatus.OK.value(), "OK", null), HttpStatus.OK);
    }

}
