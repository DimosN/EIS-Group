package com.task.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import com.task.domain.Sentence;

/**
 * @author Dmitry Novikov
 * @since 2020-05-31
 */
public interface SentenceRepository extends CrudRepository<Sentence, UUID> {

    @Query("SELECT * FROM sentence WHERE text LIKE :text")
    List<Sentence> findSentencesByText(@Param("text") String text);

    @Query("SELECT * FROM sentence")
    List<Sentence> findAllSentences();

}
