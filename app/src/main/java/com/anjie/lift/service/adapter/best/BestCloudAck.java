package com.anjie.lift.service.adapter.best;

import android.content.Context;

import com.anjie.common.log.LogX;
import com.anjie.common.system.SystemPropertiesProxy;
import com.anjie.lift.app.AppContext;
import com.anjie.lift.config.ConfigManager;
import com.shbst.androiddevicesdk.DeviceSDK;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 这个类主要用户上报设备当前的参数配置
 */
class BestCloudAck
{
    /**
     * 日志标签
     */
    private static final String TAG = "BestCloudAck";

    /**
     * 单实例
     */
    private static BestCloudAck instance;

    /**
     * 改为单例模式
     * 2018.07.19
     */
    public synchronized static BestCloudAck getInstance()
    {
        if (instance == null)
        {
            instance = new BestCloudAck();
        }
        return instance;
    }

    private BestCloudAck()
    {

    }

    /**
     * 创建响应服务器消息,上报设备端测当前信息
     */
    void createACK(String uid, int type)
    {
        JSONObject jsonObject = new JSONObject();
        JSONObject items = new JSONObject();
        try
        {
            jsonObject.put("time", String.valueOf(System.currentTimeMillis()));

            //2018.05.02  上报的json的格式改了，为String类型
            items.put(BestCloudConfig.full_screen, String.valueOf(getFullScreenValue()));
            //items.put(BestCloudConfig.screen_save_time, String.valueOf(ConfigManager.getInstance().getScreenSaveTime()));
            items.put(BestCloudConfig.sys_brightness, String.valueOf(ConfigManager.getInstance().getStandByBrightness()));
            items.put(BestCloudConfig.brightness, String.valueOf(ConfigManager.getInstance().getBrightness()));
            items.put(BestCloudConfig.volume, String.valueOf(ConfigManager.getInstance().getVolume()));

            items.put(BestCloudConfig.hidden_title, String.valueOf(getHiddenTitleValue()));
            items.put(BestCloudConfig.hidden_date, String.valueOf(getHiddenTimeValue()));
            items.put(BestCloudConfig.hidden_scrollText,String.valueOf(getHiddenScrollTextValue()));
//            // 横竖屏
            items.put(BestCloudConfig.display_type, String.valueOf(getDisplayType()));
            items.put(BestCloudConfig.time_format, String.valueOf(getTimeFormat()));
            items.put(BestCloudConfig.date_format, String.valueOf(getDateFormat()));

//            // 图片的显示时间
            items.put(BestCloudConfig.image_interval,String.valueOf(ConfigManager.getInstance().getImageInterval()));

            //2018.04.26 上报流量情况 包括流量使用的多少，云端设置的流量上限，流量阈值
            items.put(BestCloudConfig.usedDataFlow, String.valueOf(FlowManager.getInstance().getFlowUsed()));
            items.put(BestCloudConfig.dataFlowLimit, String.valueOf(FlowManager.getInstance().getFlowLimit()));
            items.put(BestCloudConfig.sizeThreshold, String.valueOf(FlowManager.getInstance().getFlowThreshold()));

            // 封装参数报文内容格式
            jsonObject.put("items", items);
        }
        catch (JSONException e)
        {
            LogX.e(TAG, "createACK meet JSONException.", e);
        }
        // 调用SDK上报当前端测配置信息
        DeviceSDK.getInstance().sendMessage(jsonObject.toString().getBytes(), type, " ");
    }

    /**
     * 获取当前屏幕显示方式0-横屏,1-竖屏
     * 
     * @return 返回横竖屏的值
     */
    private int getDisplayType()
    {
        Context context = AppContext.getInstance().getContext();
        String type = SystemPropertiesProxy.get(context, "persist.sys.hwrotation");
        int displayType = 0;
        try
        {
            displayType = Integer.parseInt(type);
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Screen Display type get value failed:" + type);
        }

        if (displayType == 0 || displayType == 180)//2018.07.11
        {
            // 客户端的横竖屏根选择角度有关系
            return 0;
        }
        else if (displayType == 90 || displayType == 270)
        {
            return 1;
        }
        else {
            return 0;
        }
    }

    /**
     * 是否全屏的值
     * 
     * @return 全屏值
     */
    private int getFullScreenValue()
    {
        boolean isFullScreen = ConfigManager.getInstance().isFullScreenMode();
        // 是否全屏 1-全屏，0-非全屏
        return isFullScreen ? 1 : 0;
    }

    /**
     * 获取是否隐藏标题的值
     * 
     * @return 值
     */
    private int getHiddenTitleValue()
    {
        boolean isHiddenTitle = ConfigManager.getInstance().isHiddenTitle();
        return isHiddenTitle ? 0 : 1;
    }

    /**
     * 获取是否隐藏滚动广告字幕的值
     * 
     * @return 值
     */
    private int getHiddenScrollTextValue()
    {
        boolean isHiddenScrollText = ConfigManager.getInstance().isHiddenScrollText();
        return isHiddenScrollText ? 0 : 1;
    }

    /**
     * 是否隐藏时间区域的值
     * 
     * @return 值
     */
    private int getHiddenTimeValue()
    {
        boolean isHiddenTimer = ConfigManager.getInstance().isHiddenTimer();
        return isHiddenTimer ? 0 : 1;
    }


    private int getTimeFormat(){
        String timeFormat = ConfigManager.getInstance().getTimeFormat();
        return timeFormat.equals( "kk:mm") ? 1 :0;
    }

    private int getDateFormat(){
        String dateFormat = ConfigManager.getInstance().getTimeFormat();
        return dateFormat.equals( "yyyy.MM.dd") ? 1 :0;
    }
}
