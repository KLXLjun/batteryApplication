package moe.huyaoi.batteryapplication.event;

import java.util.ArrayList;

public class TextEventSource {
    private ArrayList<TextEventListener> listeners = new ArrayList<>();

    // 注册监听器
    public void addListener(TextEventListener listener) {
        listeners.add(listener);
    }

    // 注销监听器
    public void removeListener(TextEventListener listener) {
        listeners.remove(listener);
    }

    // 触发事件
    public void triggerEvent(TextEvent event) {
        for (TextEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
