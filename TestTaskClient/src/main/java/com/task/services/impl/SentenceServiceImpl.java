package com.task.services.impl;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.task.domain.Sentence;
import com.task.repositories.SentenceRepository;
import com.task.serviceobjects.TextSO;
import com.task.services.SentenceService;

import static java.util.stream.Collectors.toList;

/**
 * @author Dmitry Novikov
 * @since 2020-06-01
 */
@Service
public class SentenceServiceImpl implements SentenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentenceServiceImpl.class);

    @Autowired
    private SentenceRepository sentenceRepository;

    @Override
    public List<TextSO> getSentences(String searchString) {
        List<Sentence> sentences;
        if (!StringUtils.isEmpty(searchString)) {
            sentences = sentenceRepository.findSentencesByText("%" + searchString + "%");
            LOGGER.debug("{} sentences were found for search string '{}'.", sentences.size(), searchString);
        } else {
            sentences = sentenceRepository.findAllSentences();
            LOGGER.debug("{} sentences were found.", sentences.size());
        }

        List<TextSO> results = sentences.stream().map(sentence -> new TextSO(sentence)).collect(toList());
        return results;
    }

}
