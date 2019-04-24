package rodrigue.handlers;

import rodrigue.dto.MessageResponseDTO;
import rodrigue.handlers.exceptions.RabbitMQException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

import static java.util.stream.Collectors.toList;

@ControllerAdvice
class Handler {
    private static final Logger logger = LoggerFactory.getLogger(Handler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<MessageResponseDTO> processValidationError(MethodArgumentNotValidException ex) {
        logger.error("Validation errors caught, messages sent back to client.");

        BindingResult result = ex.getBindingResult();
        List<String> validationErrors = result.getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new MessageResponseDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        null,
                        "Your request is invalid, please fix the validation errors."
                )
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({RabbitMQException.class, SocketTimeoutException.class, SocketException.class})
    ResponseEntity<MessageResponseDTO> rabbitMQUnresponsive(Throwable ex) {
        logger.error("RabbitMQ unresponsive: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                new MessageResponseDTO(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        null,
                        "Could not establish connection, please try again later."
                )
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<MessageResponseDTO> httpMessageNotReadable(Throwable ex) {
        logger.error("Malformed Request received: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new MessageResponseDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        null,
                        "Malformed request, please make sure the headers and body are valid."
                )
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    ResponseEntity<MessageResponseDTO> fallThroughAll(Throwable ex) {
        logger.error("Throwable exception of type [{}] caught during application runtime with message: {}", ex.getClass(), ex.getMessage());

        if (logger.isDebugEnabled()) {
            ex.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new MessageResponseDTO(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        null,
                        ex.getMessage()
                )
        );
    }
}
