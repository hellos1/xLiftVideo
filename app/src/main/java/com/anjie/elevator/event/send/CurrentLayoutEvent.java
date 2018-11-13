package com.anjie.elevator.event.send;

import com.anjie.elevator.event.base.BaseSenderEvent;

/**
 * 屏上线后的主动发的指令，向云端主动发送当前布局配置
 */
public class CurrentLayoutEvent extends BaseSenderEvent {
    public CurrentLayoutEvent() {
        super("iotevent.config.curlayout");
    }

}
