package com.anjie.lift.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.anjie.lift.app.AppContext;

/**
 * 单实例配置管理
 */
public class ConfigManager
{
    /**
     * 单实例
     */
    private static final ConfigManager instance = new ConfigManager();

    /**
     * 全局配置,处理各种共同配置参数,不需要app程序自己维护配置文件
     */
    private static final String SP_NAME = "GlobalConfig";

    /**
     * SP
     */
    private SharedPreferences sp = null;

    /**
     * 一级节能模式
     */
    private SavePowerMode firstStage;

    /**
     * 二级节能模式
     */
    private SavePowerMode secondStage;

    /**
     * 私有构造
     */
    private ConfigManager()
    {
        Context context = AppContext.getInstance().getContext();
        sp = context.getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE);
    }

    /**
     * 获取单实例
     *
     * @return
     */
    public static ConfigManager getInstance()
    {
        return instance;
    }

    /**
     * 全局声音音量值
     */
    public static final String SP_KEY_VOLUME = "volume";

    /**
     * 全局亮度值
     */
    private static final String SP_KEY_BRIGHTNESS = "brightness";

    /**
     * StandBy Brightness
     */
    private static final String SP_KEY_SYS_BRIGHTNESS = "sysBrightness";

    /**
     *
     */
    private static final String SP_KEY_IMAGE_INTERVAL = "image_interval";

    /**
     * 标题内容
     */
    private static final String SP_KEY_TITLE = "title";

    /**
     * 滚动字幕内容
     */
    private static final String SP_KEY_SCROLL_TEXT = "scrollText";

    /**
     * 是否全屏模式
     */
    public static final String SP_KEY_FULL_MODE = "isFullScreenMode";

    /**
     * 一级节能模式
     */
    private static final String SP_KEY_FIRST_STAGE = "firstSavePowerMode";

    /**
     * 二级节能模式
     */
    private static final String SP_KEY_SEC_STAGE = "secondSavePowerMode";

    /**
     * 是否隐藏时间区域
     */
    private static final String SP_KEY_IS_HIDDEN_TIME = "isHiddenTime";

    /**
     * 是否隐藏滚动文字区域
     */
    private static final String SP_KEY_IS_HIDDEN_SCROLL_TEXT = "isHiddenScrollText";

    /**
     * 是否隐藏标题
     */
    private static final String SP_KEY_IS_HIDDEN_TITLE = "isHiddenTitle";

    /**
     * Screen saver time
     */
    private static final String SP_KEY_SCREEN_SAVE_TIME = "screen_save_time";

    /**
     * 缓存的下载资源
     */
    private static final String SP_KEY_DOWNLOAD_PLAY_RES = "downloadMediaResCache";

    /**
     * 时间格式
     */
    private static final String SP_KEY_TIME_FORMAT = "time_format";

    /**
     * 日期格式
     */
    private static final String SP_KEY_DATA_FORMAT = "date_format";


    /**
     * 屏幕大小 10.4 or 15 (存储的值为104/150)
     */
    //2018.07.11
    private static final String SCREEN_SIZE_MSG = "app_screen_size";


    /**
     * 获取标题内容
     *
     * @return
     */
    public String getTitle()
    {
        return sp.getString(SP_KEY_TITLE, null);
    }

    /**
     * 获取滚动广告文字
     *
     * @return
     */
    public String getScrollText()
    {
        return sp.getString(SP_KEY_SCROLL_TEXT, null);
    }

    /**
     * 获取一级节能模式
     *
     * @return
     */
    public SavePowerMode getFirstStage()
    {
        if (firstStage == null)
        {
            String jsonText = sp.getString(SP_KEY_FIRST_STAGE, null);
            if (!TextUtils.isEmpty(jsonText))
            {
                firstStage = SavePowerMode.newInstance(jsonText);
            }
        }
        return firstStage;
    }

    /**
     * 获取二级节能模式
     *
     * @return
     */
    public SavePowerMode getSecondStage()
    {
        if (secondStage == null)
        {
            String jsonText = sp.getString(SP_KEY_SEC_STAGE, null);
            if (!TextUtils.isEmpty(jsonText))
            {
                secondStage = SavePowerMode.newInstance(jsonText);
            }
        }
        return secondStage;
    }

    /**
     * 保存节能模式值
     *
     * @param savePowerMode
     * @param level
     */
    public void setSavePowerMode(SavePowerMode savePowerMode, int level)
    {
        if (savePowerMode == null)
        {
            return;
        }
        if (level == 1)
        {
            this.firstStage = savePowerMode;
            sp.edit().putString(SP_KEY_FIRST_STAGE, savePowerMode.toJSON()).apply();
        }
        else if (level == 2)
        {
            this.secondStage = savePowerMode;
            sp.edit().putString(SP_KEY_SEC_STAGE, savePowerMode.toJSON()).apply();
        }
    }

    /**
     * 是否隐藏标题
     */
    public void hiddenTitle(boolean isHidden)
    {
        if (isHidden == true)
        {
            isHidden = false;
        }
        else
        {
            isHidden = true;
        }
        sp.edit().putBoolean(SP_KEY_IS_HIDDEN_TITLE, isHidden).apply();
    }

    /**
     * 是否隐藏滚动文字
     */
    public void hiddenScrollText(boolean isHidden)
    {
        if (isHidden == true)
        {
            isHidden = false;
        }
        else
        {
            isHidden = true;
        }
        sp.edit().putBoolean(SP_KEY_IS_HIDDEN_SCROLL_TEXT, isHidden).apply();
    }

    /**
     * 是否隐藏日期
     */
    public void hiddenDate(boolean isHidden)
    {
        if (isHidden == true)
        {
            isHidden = false;
        }
        else
        {
            isHidden = true;
        }
        sp.edit().putBoolean(SP_KEY_IS_HIDDEN_TIME, isHidden).apply();
    }

    /**
     * 获取时间的格式
     *
     * @return
     */
    public String getTimeFormat()
    {
        return sp.getString(SP_KEY_TIME_FORMAT, "kk:mm"); //hh:mm a   kk:mm
    }

    public void setTimeFormat(String format)
    {
        if (!TextUtils.isEmpty(format))
        {
            sp.edit().putString(SP_KEY_TIME_FORMAT, format).apply();
        }
    }

    /**
     * 获取日期的格式
     *
     * @return
     */
    public String getDateFormat()
    {
        return sp.getString(SP_KEY_DATA_FORMAT, "yyyy.MM.dd");// MM-dd-yyyy   yyyy-MM-dd
    }

    public void setDateFormat(String format)
    {
        if (!TextUtils.isEmpty(format))
        {
            sp.edit().putString(SP_KEY_DATA_FORMAT, format).apply();
        }
    }

    /**
     * 是否隐藏标题
     * @return 是否隐藏标题
     */
    public boolean isHiddenTitle()
    {
        return sp.getBoolean(SP_KEY_IS_HIDDEN_TITLE, false);
    }

    /**
     * 是否隐藏滚动文字
     * 
     * @return 是否隐藏滚动文字
     */
    public boolean isHiddenScrollText()
    {
        return sp.getBoolean(SP_KEY_IS_HIDDEN_SCROLL_TEXT, false);
    }

    /**
     * 是否隐藏时间区域
     * 
     * @return 是否隐藏时间区域
     */
    public boolean isHiddenTimer()
    {
        return sp.getBoolean(SP_KEY_IS_HIDDEN_TIME, false);
    }

    /**
     * 设置是否全屏模式
     *
     * @param isFullScreenMode
     *            是否全屏模式
     */
    public void setFullScreenMode(boolean isFullScreenMode)
    {
        sp.edit().putBoolean(SP_KEY_FULL_MODE, isFullScreenMode).apply();
    }

    /**
     * 是否全屏模式
     *
     * @return 是否全屏模式
     */
    public boolean isFullScreenMode()
    {
        return sp.getBoolean(SP_KEY_FULL_MODE, false);
    }

    /**
     * 设置标题内容
     * 
     * @param title
     *            标题内容
     */
    public void setTitle(String title)
    {
        sp.edit().putString(SP_KEY_TITLE, title).apply();
    }

    /**
     * 设置滚动文字的内容
     * 
     * @param scrollText
     *            滚动文字
     */
    public void setScrollText(String scrollText)
    {
        sp.edit().putString(SP_KEY_SCROLL_TEXT, scrollText).apply();
    }

    public String getMediaDownloadCache()
    {
        return sp.getString(SP_KEY_DOWNLOAD_PLAY_RES, null);
    }

    public void updateMediaDownloadCache(String jsonData)
    {
        sp.edit().putString(SP_KEY_DOWNLOAD_PLAY_RES, jsonData).apply();
    }

    /**
     * 设置声音的值
     * 
     * @param value
     *            声音值
     */
    public void setVolume(int value)
    {
        sp.edit().putInt(SP_KEY_VOLUME, value).apply();
    }

    /**
     * 获取声音记录值
     * 
     * @return 声音记录值
     */
    public int getVolume()
    {
        return sp.getInt(SP_KEY_VOLUME, 15);
    }//Change to default value 15

    /**
     * 设置屏幕亮度值
     * 
     * @param brightness
     *            屏幕亮度
     */
    public void setBrightness(int brightness)
    {
        sp.edit().putInt(SP_KEY_BRIGHTNESS, brightness).apply();
    }

    /**
     * 获取系统存储的亮度
     * 
     * @return 屏幕亮度记录值
     */
    public int getBrightness()
    {
        return sp.getInt(SP_KEY_BRIGHTNESS, 30);
    }

    /**
     * 设置Standby brightness
     * 
     * @param brightness
     *            亮度值
     */
    public void setStandByBrightness(int brightness)
    {
        sp.edit().putInt(SP_KEY_SYS_BRIGHTNESS, brightness).apply();
    }

    /**
     * 获取 Standby brightness
     */
    public int getStandByBrightness()
    {
        return sp.getInt(SP_KEY_SYS_BRIGHTNESS, 50);
    }

    /**
     * 设置图片播放周期时间
     * 
     * @param seconds
     *            秒
     */
    public void setImageInterval(int seconds)
    {
        sp.edit().putInt(SP_KEY_IMAGE_INTERVAL, seconds).apply();
    }

    /**
     * 图片播放间隔周期(默认时间3秒)
     */
    public int getImageInterval()
    {
        return sp.getInt(SP_KEY_IMAGE_INTERVAL, 0);
    }

    public void setScreenSaveTime(int seconds)
    {
        sp.edit().putInt(SP_KEY_SCREEN_SAVE_TIME, seconds).apply();
    }

    public int getScreenSaveTime()
    {
        return sp.getInt(SP_KEY_SCREEN_SAVE_TIME, 180);
    }

    /**
     * 屏幕大小的信息
     */
    //2018.07.11
    public void setScreenSizeMsg(int size)
    {
        sp.edit().putInt(SCREEN_SIZE_MSG, size).apply();
    }

    public int getScreenSizeMsg()
    {
        return sp.getInt(SCREEN_SIZE_MSG,0);
    }

    public void removeScreenSizeMsg(){
        sp.edit().remove(SCREEN_SIZE_MSG).apply();
    }

    /**
     * 恢复到默认设置
     */
    public void resetDefault()
    {
        // 删除声音和亮度
        sp.edit().remove(SP_KEY_VOLUME).apply();
        sp.edit().remove(SP_KEY_BRIGHTNESS).apply();

        // 删除标题和滚动字幕
        sp.edit().remove(SP_KEY_TITLE).apply();
        sp.edit().remove(SP_KEY_SCROLL_TEXT).apply();

        // 删除全屏模式
        sp.edit().remove(SP_KEY_FULL_MODE).apply();

        // 删除一级，二级节能模式
        sp.edit().remove(SP_KEY_FIRST_STAGE).apply();
        sp.edit().remove(SP_KEY_SEC_STAGE).apply();

        // 删除隐藏模式记录
        sp.edit().remove(SP_KEY_IS_HIDDEN_TIME).apply();
        sp.edit().remove(SP_KEY_IS_HIDDEN_SCROLL_TEXT).apply();
        sp.edit().remove(SP_KEY_IS_HIDDEN_TITLE).apply();

        // 删除缓存的下载资源
        sp.edit().remove(SP_KEY_DOWNLOAD_PLAY_RES).apply();

        //删除screen size msg
        //sp.edit().remove(SCREEN_SIZE_MSG).apply();
    }
}
