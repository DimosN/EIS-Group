package com.task.processors;

import javax.annotation.PostConstruct;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.rx.ReactiveCamel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import com.task.domain.Sentence;
import com.task.repositories.SentenceRepository;

/**
 * Processor which receives sentences from Kafka and stores them in Cassandra.
 *
 * @author Dmitry Novikov
 * @since 2020-05-31
 */
@Component
public class SentenceProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentenceProcessor.class.getName());

    @Autowired
    private Environment env;

    @Autowired
    private SentenceRepository sentenceRepository;

    @PostConstruct
    public void init() {
        CamelContext context = new DefaultCamelContext();
        ReactiveCamel camel = new ReactiveCamel(context);

        // initializing for listening of Kafka topic with sentences
        String sentencesTopic = env.getProperty("kafka.sentences.topic");
        String bootstrapServers = env.getProperty("kafka.bootstrapServers");
        String uri = String.format("kafka:%s?brokers=%s", sentencesTopic, bootstrapServers);
        camel.toObservable(uri).subscribe(e -> {
            // handling of receiving message with sentence from Kafka
            Object body = e.getBody();
            if (body instanceof String) {
                // saving of sentence in Cassandra
                String text = (String)body;
                sentenceRepository.save(new Sentence(text));
                LOGGER.debug("New sentence '{}' was received from topic '{}' and stored in Cassandra.", text, sentencesTopic);
            } else {
                LOGGER.error("Not string object was received from topic '{}'.", sentencesTopic);
            }
        });
    }

}
