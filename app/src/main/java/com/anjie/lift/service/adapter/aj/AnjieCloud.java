package com.anjie.lift.service.adapter.aj;

import com.anjie.common.log.LogX;
import com.anjie.lift.app.AppContext;
import com.anjie.lift.download.ApkDownloadManager;
import com.anjie.lift.download.MediaDownloadManager;
import com.anjie.lift.download.ViewDownloadManager;
import com.anjie.lift.service.adapter.CloudAdapter;
import com.anjie.lift.utils.AppUtils;

import android.content.Context;

/**
 * 安杰云服务
 */
public class AnjieCloud implements CloudAdapter
{
    /**
     * 日志标签
     */
    private static final String TAG = "AnjieCloud";

    /**
     * APK版本管理
     */
    private ApkDownloadManager apkManager = null;

    /**
     * 视图布局更新管理
     */
    private ViewDownloadManager viewManager = null;

    /**
     * 媒体资源管理
     */
    private MediaDownloadManager mediaManager = null;

    /**
     * 安杰云
     */
    public AnjieCloud()
    {
        Context context = AppContext.getInstance().getContext();
        apkManager = new ApkDownloadManager(context);
        mediaManager = new MediaDownloadManager(context);
        viewManager = new ViewDownloadManager(context);
    }

    /**
     * 向服务器请求播放列表
     */
    private void validPlayList()
    {
        mediaManager.requestServerMediaInfo();
    }

    /**
     * 向服务器请求UI视图布局信息
     */
    private void validViewVersion()
    {
        viewManager.requestServerViewInfo();
    }

    /**
     * 请求服务器版本信息
     */
    private void validAppVersion()
    {
        apkManager.requestServerVersionInfo();
    }

    /**
     * 校验任务,使用锁
     */
    private synchronized void validTask()
    {
        boolean isConnected = AppUtils.isNetworkConnected(AppContext.getInstance().getContext());
        if (!isConnected)
        {
            LogX.e(TAG, "Network is not available.");
            // 网络未连接,返回
            return;
        }
        LogX.d(TAG, "Background Task:begin to check sync with server.");
        // 检测播放列表
        validPlayList();
        // 检测视图版本
        //validViewVersion();
        // 检测App的版本
        //validAppVersion();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onSyncWithServer()
    {
        validTask();
    }

    @Override
    public void onDestroy() {

    }
}
