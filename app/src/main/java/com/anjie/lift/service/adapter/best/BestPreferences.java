package com.anjie.lift.service.adapter.best;

import android.content.Context;
import android.content.SharedPreferences;

import com.anjie.lift.app.AppContext;

import static com.anjie.lift.config.ConfigManager.SP_KEY_VOLUME;

/**
 * Best云操作的相关持久化操作记录(根配置下发无关)
 */
public class BestPreferences
{
    /**
     * 日志标签
     */
    private static final String TAG = "BestPreferences";
    /**
     * Best云的SP
     */
    private static final String SP_NAME = "BestPreferences";
    /**
     * SharedPreferences
     */
    private SharedPreferences preferences;

    /**
     * 升级APK的URL下载地址
     */
    private static final String KEY_UPDATE_URL = "UPDATE_URL";

    public BestPreferences()
    {
        Context context = AppContext.getInstance().getContext();
        preferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 缓存升级APK的URL
     * 
     * @param url
     * @return
     */
    public boolean cacheUpdateUrl(String url)
    {
        return preferences.edit().putString(KEY_UPDATE_URL, url).commit();
    }

    /**
     * 缓存升级APK的URL
     *
     * @param url
     * @return
     */
    public void ClearCache()
    {
         preferences.edit().remove(SP_KEY_VOLUME).apply();
    }
    /**
     * 获取缓存升级的URL地址
     * 
     * @return
     */
    public String getCacheUpdateURL()
    {
        return preferences.getString(KEY_UPDATE_URL, null);
    }
}
