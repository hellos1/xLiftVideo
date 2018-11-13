package com.anjie.lift.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.anjie.common.log.LogX;
import com.anjie.lift.app.AppContext;
import com.anjie.lift.service.adapter.CloudAdapter;
import com.anjie.lift.service.adapter.best.BestCloud;
import com.anjie.lift.service.adapter.best.FlowManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 后台资源服务
 */
public class ResourceService extends Service
{
    /**
     * 日志标签
     */
    private static final String TAG = "ResourceService";

    /**
     * 命令的类型
     */
    public static final String COMMAND_KEY = "CommandKeyType";

    /**
     * 网络连接上
     */
    public static final int COMMAND_NETWORK_CONNECTED = 1;

    /**
     * 云端资源同步方法
     */
    private CloudAdapter cloudAdapter = null;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        // Create方法只会被调用一次，创建的时候

        // 初始化Context(非必须，LiftApplication已经做过初始化)
        AppContext.getInstance().init(getApplicationContext());

        cloudAdapter = new BestCloud();
        cloudAdapter.onCreate();
        FlowManager.getInstance().checkFlow();

        // 定时任务
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        // 1小时后执行,以后每小时执行一次
        // executor.scheduleAtFixedRate(new ScheduleTask(), 60 * 60, 60 * 60,
        // TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(new ScheduleTask(), 10, 240, TimeUnit.SECONDS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        // onCreate方法调用完后会执行该方法
        // 当Service已经创建在后台运行,还有其他地方创建该Server,也会执行该方法，但是不会调用onCreate,因为Service只会有一份
        LogX.d(TAG, "onStartCommand()");
        if (intent != null && intent.getIntExtra(COMMAND_KEY, -1) == COMMAND_NETWORK_CONNECTED)
        {
            // 如果网络连接上了,检查任务
            validCloudTask();
        }
        // START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。
        // 随后系统会尝试重新创建service，由于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。
        // 如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        LogX.d(TAG, "onDestroy():" + this);
        if (cloudAdapter != null)
        {
            cloudAdapter.onDestroy();
        }
    }

    /**
     * 定时任务
     */
    private class ScheduleTask implements Runnable
    {
        @Override
        public void run()
        {
            validCloudTask();
        }
    }

    /**
     * 检测云端任务接口
     */
    private void validCloudTask()
    {
        if (cloudAdapter != null)
        {
            cloudAdapter.onSyncWithServer();
        }
    }
}
