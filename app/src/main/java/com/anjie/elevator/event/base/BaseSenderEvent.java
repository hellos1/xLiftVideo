package com.anjie.elevator.event.base;

import com.anjie.common.log.LogX;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseSenderEvent extends BaseEvent {
    private String TAG = "";

    public BaseSenderEvent(String eventName) {
        super(eventName);
    }

    public JSONObject buildEvent() throws JSONException {
        JSONObject baseDataJson = new JSONObject();
        baseDataJson.put("eventid", eventId);
        baseDataJson.put("eventsrcid", eventSrcId);
        baseDataJson.put("eventsenderid", eventSenderId);
        baseDataJson.put("eventname", getEventName());
        // TODO: 2018/11/12
        baseDataJson.put("eventtimestamp", "");
        return baseDataJson;
    }
}
