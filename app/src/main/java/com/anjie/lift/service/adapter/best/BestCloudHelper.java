package com.anjie.lift.service.adapter.best;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anjie.common.log.LogX;
import com.anjie.common.system.SystemPropertiesProxy;
import com.anjie.lift.app.AppContext;
import com.anjie.lift.app.BroadcastCenter;
import com.anjie.lift.config.ConfigManager;
import com.anjie.lift.manager.ControlCenter;
import com.anjie.lift.manager.MPlayerManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Best云辅助类
 */
final class BestCloudHelper
{
    /**
     * 日志标签
     */
    private static final String TAG = "BestCloud";

    /**
     * 处理MqttAdapter.onReceiveServerMsg(int result, String serverMsg) 收到的消息
     *
     * @param jsonData JSON数据
     */
    void handleOnReceiveServerMsg(String jsonData)
    {
        if (TextUtils.isEmpty(jsonData))
        {
            return;
        }
        JSONObject jsonObject;
        try
        {
            jsonObject = new JSONObject(jsonData);
        }
        catch (Exception e)
        {
            LogX.e(TAG, "handleOnReceiveServerMsg meet exception.", e);
            return;
        }

        if (jsonObject.has("firmware_type")
                && jsonObject.has("hardware_version")
                && jsonObject.has("firmware_version")
                && jsonObject.has("firmware_url"))
        {
            String version = jsonObject.optString("firmware_version", null);
            String url = jsonObject.optString("firmware_url", null);
            handleUpgrade(version, url);
            //2018.05 此处可在 固件升级时对url进行甄别   选取vendor(厂商)作为区别
//            ConfigManager.getInstance().setScreenSizeMsg(150);
            String vendor = jsonObject.optString("vendor", null);
            if (vendor.startsWith("AJ"))
            {
                //如果时AJ开头就进行更新
            }
        }
        else
        {
            LogX.w(TAG, "handleOnReceiveServerMsg know jsonData:" + jsonData);
        }

    }

    /**
     * 处理MqttAdapter.onReceive()收到的jsonMessage
     *
     * @param jsonData JSON数据
     */
    void handlerReceiveMQTTMessage(String jsonData)
    {
        LogX.d(TAG,"LIUCHUN"+jsonData);
        if (TextUtils.isEmpty(jsonData) || jsonData.length() <= 2)
        {
            // JSON数据格式至少2
            return;
        }
        JSONObject jsonObject;
        try
        {
            jsonObject = new JSONObject(jsonData);
        }
        catch (Exception e)
        {
            LogX.e(TAG, "handlerReceiveMQTTMessage meet exception.", e);
            return;
        }

        if (jsonObject.has("idtype") && jsonObject.has("type") && jsonObject.has("resources"))
        {
            // 解析服务器下发资源(标题,滚动字幕,播放图片,播放视频,网页)
            handleResource(jsonObject);
        }
        else if (jsonObject.has("did") && jsonObject.has("items"))
        {
            // 解析处理服务器下发的配置信息
            handleConfigValue(jsonObject.optJSONObject("items"));
        }
        else
        {
            LogX.w(TAG, "Unknow mqtt message content.");
        }
    }

    /**
     * 处理升级(固件升级，APK升级)
     * 
     * @param version 版本信息
     * @param url 下载地址
     */
    private void handleUpgrade(String version, String url)
    {
        LogX.i(TAG, "version:" + version + ",url:" + url);
        BestVersionManager.getInstance().updateApk(version, url);
    }

    /**
     * 处理资源类型
     *
     * @param jsonObject JSON对象
     */
    private void handleResource(JSONObject jsonObject)
    {
        List<BestResItem> playItemList = new ArrayList<BestResItem>();
        JSONArray jsonArrays = jsonObject.optJSONArray("resources");
        if (jsonArrays != null && jsonArrays.length() > 0)
        {
            BestResItem resItem;
            for (int i = 0; i < jsonArrays.length(); i++)
            {
                resItem = BestResItem.getBestResItem(jsonArrays.optJSONObject(i));
                LogX.d(TAG, "BestResItem:" + resItem);
                if (resItem != null)
                {
                    switch (resItem.getType()){
                        case Title:
                            ConfigManager.getInstance().setTitle(resItem.getContent());
                            ControlCenter.getInstance().notifyTitleChange(resItem.getContent());
                            break;
                        case ScrollText:
                            ConfigManager.getInstance().setScrollText(resItem.getContent());
                            ControlCenter.getInstance().notifyScrollTextChange(resItem.getContent());
                            break;
                        case URL:
                            // 处理网页的URL
                            BestDownloadHelper.getInstance().handleWebViewResource(resItem.getContent());
                            break;
                        default :
                            // 播放图片或者视频的资源
                            playItemList.add(resItem);
                            break;

                    }
                }
            }
        }
        // 处理播放内容的资源下载
        BestDownloadHelper.getInstance().handlePlayResource(playItemList);
    }

    /**
     * 处理Best云下发的设备 配置信息
     *
     * @param jsonObj JSON对象
     */
    private void handleConfigValue(JSONObject jsonObj)
    {
        boolean isNeedUpdateView = false;
        int value;
        long flow;
        boolean isSetFullType = false;

        if (jsonObj.has(BestCloudConfig.reset))
        {
            // 处理服务器下发恢复出厂设置
            value = jsonObj.optInt(BestCloudConfig.reset, 0);
            LogX.d(TAG, "handle reset:" + value);
            ControlCenter.getInstance().resetFactoryMode();
            return;
        }

        // 处理是否是全屏显示
        if (jsonObj.has(BestCloudConfig.full_screen))
        {
            // Full Screen 处理服务器下发的全屏设置
            value = jsonObj.optInt(BestCloudConfig.full_screen, 0);
            LogX.d(TAG, "handle fullscreen:" + value);
            isNeedUpdateView = true;
            //ConfigManager.getInstance().setFullScreenMode(value == 1);//2018.07.19注释掉的
            if (value == 1)
            isSetFullType = true;
        }

        // 处理Screen Saver time
        if (jsonObj.has(BestCloudConfig.screen_save_time))
        {
            // 处理服务器下发的 Screen Saver time
            value = jsonObj.optInt(BestCloudConfig.screen_save_time, 180);
            ConfigManager.getInstance().setScreenSaveTime(value);
            LogX.d(TAG, "handle screentime:" + value);
        }

        // 处理Standby 亮度
        if (jsonObj.has(BestCloudConfig.sys_brightness))
        {
            // 处理服务器下发的 Standby Brightness
            value = jsonObj.optInt(BestCloudConfig.sys_brightness, 100);
            ConfigManager.getInstance().setStandByBrightness(value);
            LogX.d(TAG, "handle sybrightness:" + value);
        }

        // 处理正常亮度
        if (jsonObj.has(BestCloudConfig.brightness))
        {
            // 处理服务器下发的 Screen Brightness
            value = jsonObj.optInt(BestCloudConfig.brightness, 100);
            LogX.d(TAG, "handle brightness:" + value);
            // 保存一份值，后续节能模式恢复正常的时候需要用到这个值
            ConfigManager.getInstance().setBrightness(value);
            ControlCenter.getInstance().setBrightness(value);
        }

        // 处理声音
        if (jsonObj.has(BestCloudConfig.volume))
        {
            // 处理服务器下发的声音 volume
            value = jsonObj.optInt(BestCloudConfig.volume, 100);
            LogX.d(TAG, "handle volume:" + value);
            ConfigManager.getInstance().setVolume(value);
         //   MPlayerManager.getInstance().setMediaVolume(value);
        }

        // 处理图片间隔时间
        if (jsonObj.has(BestCloudConfig.image_interval))
        {
            // Image Interval
            value = jsonObj.optInt(BestCloudConfig.image_interval, 3);
            // 保存配置数据
            ConfigManager.getInstance().setImageInterval(value);
            LogX.d(TAG, "handle imageinterval:" + value);
        }

        // 处理时间区域是否隐藏
        if (jsonObj.has(BestCloudConfig.hidden_date))
        {
            // Date 时间显示变化,刷新UI
            value = jsonObj.optInt(BestCloudConfig.hidden_date, 0);
            LogX.d(TAG, "handle date:" + value);
            ConfigManager.getInstance().hiddenDate(value == 1);
            isNeedUpdateView = true;
        }

        // 处理滚动字幕是否隐藏
        if (jsonObj.has(BestCloudConfig.hidden_scrollText))
        {
            // scroll text
            value = jsonObj.optInt(BestCloudConfig.hidden_scrollText, 0);
            LogX.d(TAG, "handle scrolltext:" + value);
            ConfigManager.getInstance().hiddenScrollText(value == 1);
            // 滚动字幕显示变化,刷新UI
            isNeedUpdateView = true;
        }

        // 处理标题是否隐藏
        if (jsonObj.has(BestCloudConfig.hidden_title))
        {
            // title
            value = jsonObj.optInt(BestCloudConfig.hidden_title, 0);
            LogX.d(TAG, "handle title:" + value);
            ConfigManager.getInstance().hiddenTitle(value == 1);
            // 标题显示变化,刷新UI
            isNeedUpdateView = true;
        }

        //2018.04 处理流量信息
        if (jsonObj.has(BestCloudConfig.usedDataFlow))
        {
            flow = jsonObj.optLong(BestCloudConfig.usedDataFlow, 0);
        }
        if (jsonObj.has(BestCloudConfig.dataFlowLimit))
        {
            flow = jsonObj.optLong(BestCloudConfig.dataFlowLimit, 0);
            if (flow != 0)
            {
                FlowManager.getInstance().setFlowLimit(flow);
            }
        }
        if (jsonObj.has(BestCloudConfig.sizeThreshold))
        {
            flow = jsonObj.optLong(BestCloudConfig.sizeThreshold, 0);
            if (flow != 0)
            {
                FlowManager.getInstance().setFlowThreshold(flow);
            }

        }


        // 处理横竖屏显示
        if (jsonObj.has(BestCloudConfig.display_type))
        {
            // Orientation 0-横屏; 1-竖屏
            value = jsonObj.optInt(BestCloudConfig.display_type, 0);

            //2018.07.19
            //只允许在15寸且为横屏的时候 才可以设置为全屏 已经包括了：横-->横(全)，竖-->横(全)的情况
            if (isSetFullType && ConfigManager.getInstance().getScreenSizeMsg() == 150 && value == 0)
            {
                ConfigManager.getInstance().setFullScreenMode(true);
                Log.e(TAG, "handleConfigValue: " + ConfigManager.getInstance().isFullScreenMode());
            }
            else
            {
                ConfigManager.getInstance().setFullScreenMode(false);
            }

            value = getRotationAngle(value);

            // 设置横竖屏 需要系统级权限
            Context context = AppContext.getInstance().getContext();
            MPlayerManager.getInstance().isInitVideo = true;
           // MPlayerManager.getInstance().stopPlayTask();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LogX.d("DisplayType", "This is the hwrotation before setting rotation :"
                    + SystemPropertiesProxy.get(context,"persist.sys.hwrotation"));

            SystemPropertiesProxy.set(context,"persist.sys.hwrotation", String.valueOf(value));

            LogX.d("DisplayType", "This is the hwrotation after setting rotation :"
                    + SystemPropertiesProxy.get(context,"persist.sys.hwrotation"));

            //2018.07.05 解决图片 全屏模式下 旋转不正确  先刷新下全屏就可以设置成全屏了
            if (ConfigManager.getInstance().isFullScreenMode())
            {
                new BroadcastCenter().refreshFullScreen();
                LogX.d(TAG, "This is refresh full screen action,in case of showing the pic full screen incorrectly");
            }

            LogX.d(TAG, "handle displaytype:" + value);
            // 横竖屏发生变化，刷新UI
            isNeedUpdateView = true;
        }

        // 处理时间格式
        if (jsonObj.has(BestCloudConfig.time_format))
        {
            value = jsonObj.optInt(BestCloudConfig.time_format, 0);
            String format;
            if (value == 1)
            {
                format = "kk:mm";
            }
            else
            {
                format = "hh:mm";

            }
            ConfigManager.getInstance().setTimeFormat(format);
            isNeedUpdateView = true;
        }

        // 处理日期格式
        if (jsonObj.has(BestCloudConfig.date_format))
        {
            value = jsonObj.optInt(BestCloudConfig.date_format, 0);
            String format;
            if (value == 1)
            {
                format = "yyyy.MM.dd";
            }
            else
            {
                format = "dd.MM.yyyy";//2018.06.22    由MM.dd.yyyy改
            }
            ConfigManager.getInstance().setDateFormat(format);
            isNeedUpdateView = true;
        }

        // 向服务器汇报当前客户端的配置
        String uid = jsonObj.optString("uid");
        if (TextUtils.isEmpty(uid))
        {
            //new BestCloudAck().createACK(uid, 1);//2018.07.19 改为singleton
            BestCloudAck.getInstance().createACK(uid, 1);
        }

        // 处理结果
        if (isNeedUpdateView)
        {
            // 广播视图发生变化
            MPlayerManager.getInstance().has_set_display.set(false);
            //休眠模式设置
            MPlayerManager.getInstance().bootPlay.getAndSet(true);
            //MPlayerManager.getInstance().setTagPTrue();
            LogX.d(TAG,"View has been changed");
            new BroadcastCenter().notifyViewChange();
        }
    }


    /**
     * 10.4 和 15 寸的旋转角度不同
     * 2018.07.19
     */
    private int getRotationAngle(int displayType)
    {
        if (ConfigManager.getInstance().getScreenSizeMsg() == 104)
        {
            if (displayType == 1)
            {
                displayType = 90;
            }
            else
            {
                displayType = 180;
            }
        }
        else
        {
            if (displayType == 1)
            {
                displayType = 270;
            }
            else
            {
                displayType = 0;
            }
        }
        return displayType;
    }

}
