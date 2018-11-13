package com.anjie.elevator.event.receive;

import com.anjie.elevator.event.base.BaseReceiverEvent;

import org.json.JSONObject;

public class ConfigSetLayoutEvent extends BaseReceiverEvent {
    public ConfigSetLayoutEvent(String eventName) {
        super(eventName);
    }

    @Override
    public void parseEvent(JSONObject serverJSON) {
        super.parseEvent(serverJSON);
        // TODO: 2018/11/12  解析子类数据
    }
}
