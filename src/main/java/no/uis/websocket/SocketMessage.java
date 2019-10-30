package no.uis.websocket;

public class SocketMessage {

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }

    private String messageType;
    private String content;
    private String sender;

    public String getType() {
        return messageType;
    }

    public void setType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}