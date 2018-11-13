package com.anjie.lift.config;

import android.text.TextUtils;

import com.anjie.common.log.LogX;

import org.json.JSONObject;

/**
 * 节能模式
 */
public final class SavePowerMode
{
    /**
     * 节能开始时间
     */
    private int time;

    /**
     * 亮度值
     */
    private int brightness;

    /**
     * 节能模式
     */
    public SavePowerMode()
    {

    }

    public int getTime()
    {
        return time;
    }

    public void setTime(int time)
    {
        this.time = time;
    }

    public int getBrightness()
    {
        return brightness;
    }

    public void setBrightness(int brightness)
    {
        this.brightness = brightness;
    }

    /**
     * 根据JSON构建实例
     * 
     * @param jsonText
     * @return
     */
    public static SavePowerMode newInstance(String jsonText)
    {
        if (TextUtils.isEmpty(jsonText))
        {
            return null;
        }
        SavePowerMode savePowerMode = new SavePowerMode();
        try
        {
            JSONObject jsonObject = new JSONObject(jsonText);
            savePowerMode.setBrightness(jsonObject.optInt("brightness", -1));
            savePowerMode.setTime(jsonObject.optInt("time", -1));
        }
        catch (Exception e)
        {
            LogX.e("", "newInstance SavePowerMode meet exception.", e);
            return null;
        }
        return savePowerMode;
    }

    public String toJSON()
    {
        JSONObject josnObj = new JSONObject();
        try
        {
            josnObj.put("time", time);
            josnObj.put("brightness", brightness);
        }
        catch (Exception e)
        {
            LogX.e("", "SavePowerMode to json meet exception.", e);
            return null;
        }
        return josnObj.toString();
    }

    @Override
    public String toString()
    {
        return "[time:" + time + ",brightness:" + brightness + "]";
    }
}
