package com.example.user.chatapplication.model;

/**
 * Created by User on 03/02/2018.
 */

public class Message {

    private String message;
    private String type;

    private String from;
    public boolean seen;
    public Long time;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isSeen() {
        return seen;
    }

    public Message(String message, String type, String from, boolean seen, Long time) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.seen = seen;
        this.time = time;
    }

    public Message(String message, boolean seen, String type, Long time) {
        this.message = message;
        this.seen  = seen;
        this.type = type;
        this.time = time;
    }

    public Message(){}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
