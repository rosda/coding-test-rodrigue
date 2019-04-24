package rodrigue.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rodrigue.dto.MessagePOJO;
import rodrigue.dto.MessageRequestDTO;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Service
public class ElasticServiceImpl implements ElasticService {

    @Value("${spring.elasticsearch.words.limit}")
    private int wordsLimit;

    @Value("${spring.rabbitmq.file.location.high}")
    private String highUseFile;

    @Value("${spring.rabbitmq.file.location.moderate}")
    private String moderateUseFile;

    private final
    Client client;

    private static final Logger logger = LoggerFactory.getLogger(ElasticServiceImpl.class);

    @Autowired
    public ElasticServiceImpl(Client client) {
        this.client = client;
    }

    @Override
    public void initialize() throws Exception {
        logger.info("Setting up elasticsearch");

        insert("1", "initiate");

        logger.info("Elasticsearch setup");
    }

    @Override
    public void saveIndex(MessageRequestDTO messageRequestDTO) {
        logger.info("Storing text to elasticsearch");

        ObjectMapper mapper = new ObjectMapper();

        try {
            MessagePOJO messagePOJO = mapper.readValue(messageRequestDTO.getPayload(), MessagePOJO.class);

            insert(messageRequestDTO.getId(), messagePOJO.getMessage());

            logger.info("Stored text to elasticsearch");
        } catch (Exception e) {
            logger.error(e.getMessage());

            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveIndex(String id, String text) {
        logger.info("Storing text to elasticsearch");

        try {
            insert(id, text);

            logger.info("Stored text to elasticsearch");
        } catch (Exception e) {
            logger.error(e.getMessage());

            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    @Override
    @Scheduled(cron = "0 0/10 * * * *")
    public void analyze() {
        logger.info("Starting elasticsearch analyzer");

        int tracker = 1;

        try {
            SearchResponse response = client.prepareSearch()
                    .setQuery(QueryBuilders.matchAllQuery())
                    .addAggregation(
                            AggregationBuilders
                                    .terms("PopularWords")
                                    .field("body.keyword")
                                    .size(wordsLimit)
                    )
                    .get();

            Terms aggregation = response.getAggregations().get("PopularWords");

            // We'll split the list in half just for this exercise. The aggregation buckets are by default sorted by
            // highest count descendant
            List<? extends Terms.Bucket> aggregationBucket = aggregation.getBuckets();

            int bucketSize = aggregationBucket.size();

            List<? extends Terms.Bucket> firstHalf = aggregationBucket.subList(0, (int) (bucketSize / 2));
            List<? extends Terms.Bucket> secondHalf = aggregationBucket.subList((int) (bucketSize / 2) , bucketSize);

            if (!firstHalf.isEmpty()) {
                FileWriter fileWriter = new FileWriter(highUseFile);
                PrintWriter printWriter = new PrintWriter(fileWriter);

                printWriter.println("[");

                // First Half
                for (Terms.Bucket entry : firstHalf) {
                    String eol = tracker++ < firstHalf.size() ? "\"," : "\"";

                    printWriter.println("\"" + entry.getKeyAsString() + eol);
                }

                printWriter.println("]");

                printWriter.close();
            }

            if (!secondHalf.isEmpty()) {
                tracker = 1; // Reset tracker

                FileWriter fileWriter = new FileWriter(moderateUseFile);
                PrintWriter printWriter = new PrintWriter(fileWriter);

                printWriter.println("[");

                // Second Half
                for (Terms.Bucket entry : secondHalf) {
                    String eol = tracker++ < secondHalf.size() ? "\"," : "\"";

                    printWriter.println("\"" + entry.getKeyAsString() + eol);
                }

                printWriter.println("]");

                printWriter.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());

            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }

        logger.info("Completed elasticsearch analyzer");
    }

    private void insert(String id, String text) throws IOException {
        int position = 1;

        // Separate each word before insertion so we can carefully perform the analyzer.
        // For the purpose of this exercise we are using space separated words but this can be configured otherwise
        for (String word : text.split(" ")) {
            client.prepareIndex("contents", "message", id + "_" + position)
                    .setSource(jsonBuilder().startObject()
                            .field("id", id + "_" + position)
                            .field("position", position)
                            .field("body", word)
                            .endObject()
                    )
                    .execute();

            position++;
        }
    }
}
