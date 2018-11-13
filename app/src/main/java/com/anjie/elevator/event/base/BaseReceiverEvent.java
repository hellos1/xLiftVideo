package com.anjie.elevator.event.base;

import org.json.JSONObject;

public abstract class BaseReceiverEvent extends BaseEvent {

    public BaseReceiverEvent(String eventName) {
        super(eventName);
    }

    public void parseEvent(JSONObject serverJSON) {
        if (serverJSON == null) {
            return;
        }
        eventId = serverJSON.optString("eventid");
        eventSrcId = serverJSON.optString("eventsrcid");
//        eventSenderId = serverJSON.optString("");
    }
}
