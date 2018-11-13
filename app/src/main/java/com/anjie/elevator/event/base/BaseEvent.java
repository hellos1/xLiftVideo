package com.anjie.elevator.event.base;

import org.json.JSONObject;

/**
 * 基本事件
 */
public abstract class BaseEvent {

    protected String eventId;

    protected String eventSrcId;

    protected String eventSenderId;

    /**
     * 事件Id
     */
    private String eventName;

    public BaseEvent(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
