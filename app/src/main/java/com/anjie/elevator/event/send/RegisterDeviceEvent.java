package com.anjie.elevator.event.send;

import com.anjie.elevator.event.base.BaseSenderEvent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 1.屏上线后的首条指令，向云端注册，并上传所有本地配置
 * 2.应用服务存DB（Screen表和Event表）
 * 3.门户服务提供URL供应用服务通知变更
 */
public class RegisterDeviceEvent extends BaseSenderEvent {
    public RegisterDeviceEvent() {
        super("iotevent.config.register");
    }

    @Override
    public JSONObject buildEvent() throws JSONException {
        JSONObject baseJSONData = super.buildEvent();

        JSONObject registerJSON = new JSONObject();
        registerJSON.put("deviceauthinfo", getDeviceAuthInfo());
        registerJSON.put("deviceserverlink", getDeviceServerLink());
        registerJSON.put("devicesystemparameter", getDeviceSystemParameter());
        registerJSON.put("devicedisplayparameter", getDeviceDisplayParameter());

        // 放入数据
        baseJSONData.put("eventdata", registerJSON);
        return baseJSONData;
    }

    private JSONObject getDeviceAuthInfo() throws JSONException {
        JSONObject deviceAuthInfoJSON = new JSONObject();
        deviceAuthInfoJSON.put("model", "");
        deviceAuthInfoJSON.put("sn", "");
        deviceAuthInfoJSON.put("mac", "");
        deviceAuthInfoJSON.put("manufactory", "");
        deviceAuthInfoJSON.put("manudate", "");
        deviceAuthInfoJSON.put("CA", "");
        deviceAuthInfoJSON.put("key", "");
        return deviceAuthInfoJSON;
    }

    private JSONObject getDeviceServerLink() throws JSONException {
        JSONObject deviceServerLinkJSON = new JSONObject();
        // TODO: 2018/11/12
        return deviceServerLinkJSON;
    }

    private JSONObject getDeviceSystemParameter() throws JSONException {
        JSONObject deviceSystemJSON = new JSONObject();
        // TODO: 2018/11/12
        return deviceSystemJSON;
    }

    private JSONObject getDeviceDisplayParameter() throws JSONException {
        JSONObject deviceDisplayJSON = new JSONObject();
        // TODO: 2018/11/12
        return deviceDisplayJSON;
    }
}
