package com.task.services;

import java.util.List;
import com.task.serviceobjects.TextSO;

/**
 * @author Dmitry Novikov
 * @since 2020-06-01
 */
public interface SentenceService {

    /**
     * Searches statements which contains search string(case-sensitively). If search string is empty, all sentences
     * will be returned.
     */
    List<TextSO> getSentences(String searchString);

}
