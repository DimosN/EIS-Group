package com.task.servicesobjects;

/**
 * @author Dmitry Novikov
 * @since 2020-06-01
 */
public class WordSO {

    private String text;
    private long timestamp;

    public WordSO(String text) {
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
