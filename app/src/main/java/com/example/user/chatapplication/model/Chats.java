package com.example.user.chatapplication.model;

/**
 * Created by User on 05/02/2018.
 */

public class Chats {

    private String name,messgae;
    private boolean active;

    public Chats(){}

    public Chats(String name, String messgae, boolean active) {
        this.name = name;
        this.messgae = messgae;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessgae() {
        return messgae;
    }

    public void setMessgae(String messgae) {
        this.messgae = messgae;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
