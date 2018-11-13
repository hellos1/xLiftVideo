package com.anjie.lift.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 全局的SharedPreferences文件存储类
 */
public final class AppPreference
{
    /**
     * 存储的SharedPrefernce文件名
     */
    private static final String SP_NAME = "user_sp";

    /**
     * SharedPrefernce
     */
    private SharedPreferences sp = null;

    /**
     * 保存播放列表的有效期
     */
    private static final String PLAYLIST_EXP = "playlist_exp";

    /**
     * 播放视图的有效期
     */
    private static final String VIEW_EXP = "view_exp";

    /**
     * APK版本检测时间
     */
    private static final String VERSION_EXP = "version_exp";

    /**
     * APK的版本号
     */
    private static final String APP_VERSION = "APK_VERSION";

    /**
     * 构造函数
     * 
     * @param context
     */
    public AppPreference(Context context)
    {
        //Android 的SharedPreferences文件数据记录在/data/data/com.anjie.lift/shared_prefs/下面
        sp = context.getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE);
    }

    /**
     * 验证播放列表有效期
     * 
     * @param expSeconds
     * @return
     */
    public boolean validPlayListExpire(long expSeconds)
    {
        boolean bResult = validExpireTime(PLAYLIST_EXP, expSeconds);
        return bResult;
    }

    /**
     * 更新播放列表校验时间
     * 
     * @return
     */
    public boolean updatePlayListExpire()
    {
        return sp.edit().putLong(PLAYLIST_EXP, System.currentTimeMillis()).commit();
    }

    /**
     * 校验视图的有效期
     * 
     * @param expSeconds
     * @return
     */
    public boolean validViewExpire(long expSeconds)
    {
        boolean bResult = validExpireTime(VIEW_EXP, expSeconds);
        return bResult;
    }

    /**
     * 更新视图的有效期
     * 
     * @return
     */
    public boolean updateViewExpire()
    {
        return sp.edit().putLong(VIEW_EXP, System.currentTimeMillis()).commit();
    }

    /**
     * 版本有效期检测
     * 
     * @param expSeconds
     * @return
     */
    public boolean validVersionExpire(long expSeconds)
    {
        boolean bResult = validExpireTime(VERSION_EXP, expSeconds);
        return bResult;
    }

    /**
     * 更新版本有效期检测时间
     * 
     * @return
     */
    public boolean updateVersionExpire()
    {
        return sp.edit().putLong(VERSION_EXP, System.currentTimeMillis()).commit();
    }

    /**
     * 验证是否过期
     * 
     * @param key
     * @param expSeconds
     * @return
     */
    private boolean validExpireTime(String key, long expSeconds)
    {
        boolean bResult = false;
        long lastCheckTime = sp.getLong(key, 0);
        long currentSec = System.currentTimeMillis() - lastCheckTime;
        long diff = (currentSec - lastCheckTime) / 1000;
        if (diff > expSeconds)
        {
            bResult = true;
        }
        return bResult;
    }

    public int getVersionCode()
    {
        return sp.getInt(APP_VERSION, 0);
    }

    public void cacheVersionCode(int versionCode)
    {
        sp.edit().putInt(APP_VERSION, versionCode).commit();
    }

    /**
     * 获取指定Key的value存储值
     * 
     * @param key
     * @param defValue
     * @return
     */
    public String getString(String key, String defValue)
    {
        return sp.getString(key, defValue);
    }

    /**
     * 存储指定Key的value值
     * 
     * @param key
     * @param value
     * @return
     */
    public boolean putString(String key, String value)
    {
        return sp.edit().putString(key, value).commit();
    }
}
