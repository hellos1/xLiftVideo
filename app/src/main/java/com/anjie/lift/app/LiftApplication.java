package com.anjie.lift.app;

import android.app.Application;

import com.anjie.common.log.LogX;
import com.shbst.androiddevicesdk.DeviceSDK;

/**
 * 全局应用变量
 */
public class LiftApplication extends Application
{
    /**
     * 日志标签
     */
    private static final String TAG = "LiftApplication";
    /**
     * 单实例
     */
    private static LiftApplication singleton;

    /**
     * 获取单实例
     * @return
     */
    public static LiftApplication getInstance()
    {
        return singleton;
    }

    @Override
    public final void onCreate()
    {
        super.onCreate();
        singleton = this;
        LogX.d(TAG, "onCreate()" + this);
        AppContext.getInstance().init(getApplicationContext());

        // 初始化BEST的SDK
        DeviceSDK.getInstance().init(this);
    }
}
