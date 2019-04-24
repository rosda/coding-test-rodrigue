package rodrigue.services;

import org.springframework.scheduling.annotation.Scheduled;
import rodrigue.dto.MessageRequestDTO;

public interface ElasticService {

    void initialize() throws Exception;

    void saveIndex(MessageRequestDTO messageRequestDTO);

    void saveIndex(String id, String text);

    @Scheduled(cron = "0 0/10 * * * *")
    void analyze();

}
