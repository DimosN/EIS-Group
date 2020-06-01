package com.task.controllers;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.task.serviceobjects.TextSO;
import com.task.services.SentenceService;
import com.task.services.WordService;

/**
 * Controller with client endpoints.
 * See file resources/api-doc/swagger.yaml for description.
 *
 * @author Dmitry Novikov
 * @since 2020-05-31
 */
@RestController
@RequestMapping(value = "/api/v1")
public class ClientController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private WordService wordService;

    @Autowired
    private SentenceService sentenceService;

    /**
     * Sends word to Kafka.
     *
     * @param textSO - JSON-object with text ({"text":...})
     * @return HTTP code 201, if word was sent successfully, 500 - otherwise
     */
    @RequestMapping(value = "/words/send", method = RequestMethod.POST)
    public ResponseEntity<Void> sendWord(@RequestBody TextSO textSO) {
        try {
            wordService.publishWordToKafka(textSO);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }

    /**
     * Searches sentences by search string (if it's empty, all sentences are returned)
     *
     * @return list of sentences
     */
    @RequestMapping(value = "/sentences", method = RequestMethod.GET)
    public List<TextSO> getSentences(@RequestParam(value = "searchString", required = false) String searchString) {
        List<TextSO> results = sentenceService.getSentences(searchString);
        return results;
    }

}
