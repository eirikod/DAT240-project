package no.uis.websocket;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class SocketMessage {

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }

    private String messageType;
    private Object content;
    private String sender;

    public String getType() {
        return messageType;
    }

    public void setType(String messageType) {
        this.messageType = messageType;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Map contentToMap() {
        if (content instanceof Map) {
            return (Map) content;
        }
        try {
            return new Gson().fromJson((String) content, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }
}