package rodrigue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import rodrigue.services.ElasticService;

@Component
public class ElasticRunner implements ApplicationRunner {

    private final
    ElasticService elasticService;

    private static final Logger logger = LoggerFactory.getLogger(ElasticRunner.class);

    @Autowired
    public ElasticRunner(ElasticService elasticService) {
        this.elasticService = elasticService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            elasticService.initialize();
        } catch (Exception e) {
            logger.error("Could not initialize elasticsearch! Service will run without analyzer.");

            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

}
