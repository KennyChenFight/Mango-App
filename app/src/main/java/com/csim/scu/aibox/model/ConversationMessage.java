package com.csim.scu.aibox.model;

/**
 * Created by kenny on 2018/7/21.
 */

public class ConversationMessage {
    private String content;
    private Type type;
    private String dateString;

    public ConversationMessage(String content, Type type, String dateString) {
        this.content = content;
        this.type = type;
        this.dateString = dateString;
    }
    // 判斷回答人是ROBOT還是PERSON
    public enum Type
    {
        ROBOT, PERSON, HOSPITAL
    }

    public String getContent() {
        return content;
    }

    public Type getType() {
        return type;
    }

    public String getDateString() {
        return dateString;
    }
}
