package com.csim.scu.aibox.model;

import java.io.Serializable;

public class Conversation implements Serializable {
    private String question;
    private String response;
    private String date;

    public Conversation(String question, String response, String date) {
        this.question = question;
        this.response = response;
        this.date = date;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
