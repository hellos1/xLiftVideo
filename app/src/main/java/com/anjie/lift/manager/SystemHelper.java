package com.anjie.lift.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;

import com.anjie.common.log.LogX;
import com.anjie.lift.app.AppContext;
import com.anjie.lift.config.ConfigManager;
import com.anjie.lift.config.SavePowerMode;

/**
 * Android系统属性设置
 */
public class SystemHelper
{
    /**
     * 日志标签
     */
    private static final String TAG = "SystemHelper";

    public SystemHelper()
    {

    }

    public void notifyNormalMode()
    {
        // 唤醒到屏幕正常模式
        setScreenManualMode();
        int brightness = ConfigManager.getInstance().getBrightness();
        // 设置正常的屏幕亮度
        setAndroidBrightness(brightness);
    }

    /**
     * 进入一级节能模式
     */
    public void enterFirstSavePowerMode()
    {
        SavePowerMode savePowerMode = ConfigManager.getInstance().getFirstStage();
        if (savePowerMode != null)
        {
            setAndroidBrightness(savePowerMode.getBrightness());
        }
    }

    /**
     * 进入二级节能模式
     */
    public void enterSecondSavePowerMode()
    {
        SavePowerMode savePowerMode = ConfigManager.getInstance().getSecondStage();
        if (savePowerMode != null)
        {
            setAndroidBrightness(savePowerMode.getBrightness());
        }
    }

    /**
     * 设置媒体声音大小
     * 
     * @param value
     */
    public void setVolumeLevel(int value)
    {
        Context context = AppContext.getInstance().getContext();
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // 设置媒体声音大小
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, AudioManager.FLAG_PLAY_SOUND);
    }

    /**
     * 设置亮度系统属性
     * 
     * @param value
     */
    public void setAndroidBrightness(int value)
    {
        setScreenManualMode();
        Context context = AppContext.getInstance().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        if (value > 0 && value <= 255)
        {
            try
            {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, value);
            }
            catch (Exception e)
            {
                LogX.e(TAG, "Setting screen brightness failed.", e);
            }
            LogX.d(TAG, "setSystemBrightness:" + value);
        }
    }

    /**
     * 设置屏幕亮度手动模式
     */
    private void setScreenManualMode()
    {
        Context context = AppContext.getInstance().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        try
        {
            int mode = Settings.System.getInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
            {
                Settings.System.putInt(contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Setting screen manual mode failed.", e);
        }
    }
}
