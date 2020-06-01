package com.task.domain;

import java.util.UUID;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.SASI;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * @author Dmitry Novikov
 * @since 2020-05-30
 */
@Table
public class Sentence {

    @PrimaryKey
    private UUID id;

    @Indexed(value = "textIndex")
    @SASI(indexMode = SASI.IndexMode.CONTAINS)
    private String text;

    public Sentence(String text) {
        this.id = UUID.randomUUID();
        this.text = text;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
