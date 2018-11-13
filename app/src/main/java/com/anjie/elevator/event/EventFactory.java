package com.anjie.elevator.event;

import android.text.TextUtils;

import com.anjie.elevator.event.base.BaseReceiverEvent;
import com.anjie.elevator.event.base.BaseSenderEvent;

import java.util.HashMap;
import java.util.Map;

public class EventFactory {

    private static EventFactory instance = new EventFactory();

    private Map<String, BaseReceiverEvent> eventMap = new HashMap<>();

    private EventFactory() {
        //eventMap.put("iotevent.config.setlayout",);
    }

    public BaseReceiverEvent findEvent(String eventName) {
        if (TextUtils.isEmpty(eventName)) {
            return null;
        }
        // TODO: 2018/11/12
        return null;
    }
}
