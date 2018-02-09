package com.example.user.chatapplication.model;

/**
 * Created by User on 09/02/2018.
 */

public class FriendRequest {

    String type;

    public FriendRequest(){}

    public FriendRequest(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
