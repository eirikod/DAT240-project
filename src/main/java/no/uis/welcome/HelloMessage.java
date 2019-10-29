package no.uis.welcome;

public class HelloMessage {
    private String message;

    public HelloMessage() {

    }

    public HelloMessage(String name) {
        this.message = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
