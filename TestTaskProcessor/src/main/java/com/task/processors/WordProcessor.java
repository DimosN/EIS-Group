package com.task.processors;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.rx.ReactiveCamel;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import com.task.servicesobjects.WordSO;

/**
 * Processor which receives words from Kafka and once per minute combines them in sentence (with space as separate
 * symbol) and publishes complete sentences back to Kafka to appropriate topic.
 *
 * @author Dmitry Novikov
 * @since 2020-05-31
 */
@Component
public class WordProcessor {

    private static Logger LOGGER = LoggerFactory.getLogger(WordProcessor.class.getName());

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private BlockingQueue<WordSO> wordsQueue = new LinkedBlockingQueue();

    private Properties producerProperties = new Properties();

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() throws Exception {
        initProducerProperties();

        CamelContext context = new DefaultCamelContext();
        ReactiveCamel camel = new ReactiveCamel(context);

        // initializing for listening of Kafka topic with words
        String wordsTopic = env.getProperty("kafka.words.topic");
        String bootstrapServers = env.getProperty("kafka.bootstrapServers");
        String uri = String.format("kafka:%s?brokers=%s", wordsTopic, bootstrapServers);
        camel.toObservable(uri).subscribe(e -> {
            // handling of receiving message with word from Kafka
            Object body = e.getBody();
            if (body instanceof String) {
                // adding word to blocking queue for its future appending to sentence
                String text = (String)body;
                LOGGER.debug("New word '{}' was received from topic '{}'.", text, wordsTopic);
                wordsQueue.add(new WordSO(text));
            } else {
                LOGGER.error("Not string object was received from topic '{}'.", wordsTopic);
            }
        });

        // starting thread for combining of words into sentences and publishing them to Kafka (will run once per 1
        // minute)
        scheduler.scheduleAtFixedRate(new SentencePublisher(),1, 1, TimeUnit.MINUTES);
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

    /**
     * Combinator and publisher of words.
     */
    private class SentencePublisher implements Runnable {
        @Override
        public void run() {
            createSentence();
        }

        /**
         * Extracts words from blocking queue and joins them into sentences (with spaces between).
         */
        private void createSentence() {
            long currentTimestamp = System.currentTimeMillis();
            boolean hasSentenceToSend = false;
            StringBuilder sb = new StringBuilder();
            WordSO wordSO;
            while ((wordSO = wordsQueue.peek()) != null) {
                if (wordSO != null) {
                    long wordTimestamp = wordSO.getTimestamp();
                    if (wordTimestamp <= currentTimestamp) {
                        if (!hasSentenceToSend) {
                            hasSentenceToSend = true;
                        } else {
                            sb.append(" ");
                        }
                        sb.append(wordSO.getText());
                        wordsQueue.poll();
                    } else {
                        break;
                    }
                }
            }
            // publishes sentence (if there is any to publish)
            if (hasSentenceToSend) {
                publishSentenceToKafka(sb.toString());
            }
        }

        private void publishSentenceToKafka(String sentence) {
            KafkaProducer<String, String> producer = new KafkaProducer<String, String>(producerProperties);

            String sentencesTopic = env.getProperty("kafka.sentences.topic");
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(sentencesTopic, sentence);

            producer.send(record, (RecordMetadata recordMetadata, Exception e) -> {
                if (e != null) {
                    LOGGER.error("Error on publishing message with sentence '{}' to topic '{}'. {}", sentence, sentencesTopic, e.getMessage());
                }
            });

            producer.flush();
            producer.close();

            LOGGER.debug("Sentence '{}' was published to topic '{}'.", sentence, sentencesTopic);
        }
    }

}
