package moe.huyaoi.batteryapplication.event;

public class TextEvent {
    private String text;
    public TextEvent(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }
}