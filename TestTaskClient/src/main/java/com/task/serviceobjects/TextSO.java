package com.task.serviceobjects;

import com.task.domain.Sentence;

/**
 * @author Dmitry Novikov
 * @since 2020-06-01
 */
public class TextSO {

    private String text;

    public TextSO(Sentence sentence) {
        this.text = sentence.getText();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
