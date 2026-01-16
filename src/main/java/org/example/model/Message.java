package org.example.model;

import com.google.gson.Gson;

public class Message {

    public enum Type {
        CONNECT,
        START_GAME,
        PROGRESS,
        GAME_END,
        ERROR
    }

    private Type type;
    private String data;

    public Message() {}

    public Message(Type type, String data) {
        this.type = type;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Message fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Message.class);
    }
}