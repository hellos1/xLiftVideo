package com.anjie.elevator.event.send;

import com.anjie.elevator.event.base.BaseSenderEvent;

/**
 *
 */
public class GetLayoutEvent extends BaseSenderEvent {
    public GetLayoutEvent() {
        super("iotevent.config.getlayout");
    }

}
