package no.uis.websocket;

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
}