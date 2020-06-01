package com.task.services.impl;

import java.util.Properties;
import javax.annotation.PostConstruct;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.task.serviceobjects.TextSO;
import com.task.services.WordService;

/**
 * @author Dmitry Novikov
 * @since 2020-06-01
 */
@Service
public class WordServiceImpl implements WordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordServiceImpl.class);

    private Properties producerProperties;

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        initProducerProperties();
    }

    @Override
    public void publishWordToKafka(TextSO textSO) {
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(producerProperties);

        String wordsTopic = env.getProperty("kafka.words.topic");
        String word = textSO.getText();
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(wordsTopic, word);

        producer.send(record, (RecordMetadata recordMetadata, Exception e) -> {
            if (e != null) {
                LOGGER.error("Error on publishing message with word '{}' to topic '{}'. {}", word, wordsTopic, e.getMessage());
            }
        });

        producer.flush();
        producer.close();

        LOGGER.debug("Word '{}' was published to topic '{}'.", word, wordsTopic);
    }

    /**
     * Initializes Kafka Producer settings.
     */
    private void initProducerProperties() {
        producerProperties = new Properties();
        producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty("kafka.bootstrapServers"));
        producerProperties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.ACKS_CONFIG, "all");
        producerProperties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        producerProperties.put(ProducerConfig.RETRIES_CONFIG, 3);
        producerProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);
        producerProperties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
    }

}
