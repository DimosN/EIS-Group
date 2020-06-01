package com.task.services;

import com.task.serviceobjects.TextSO;

/**
 * @author Dmitry Novikov
 * @since 2020-06-01
 */
public interface WordService {

    /**
     * Publishes word to Kafka topic (for further handling it by processor which creates sentences from words).
     */
    void publishWordToKafka(TextSO textSO);

}
