package com.anjie.lift.app;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * 广播中心
 */
public class BroadcastCenter
{
    /**
     * 视图变化广播
     */
    public static final String VIEW_CHANGE_ACTION = "com.anjie.lift.view_change_action";

    /**
     * 本地更新，列表变化
     */
    public static final String VIEW_CHANGE_ACTION_LOCAL = "com.anjie.lift.view_change_action_local";

    /**
     * 播放列表广播
     */
    public static final String PLAYLIST_CHANGE_ACTION = "com.anjie.lift.playlist_change_action";

    /**
     * 播放列表广播
     */
    public static final String PLAYLIST_CHANGE_ACTION_CLOUD = "com.anjie.lift.playlist_change_action_cloud";

    /**
     * USB同步数据
     */
    public static final String USB_SYNC_DATA_ACTION = "com.anjie.lift.usb_sync_data";

    /**
     * USB同步数据结束
     */
    public static final String USB_SYNC_DATA_FINISH_ACTION = "com.anjie.lift.usb_sync_finish_data";

    /**
     * 全屏模式图片旋转问题
     */
    public static final String REFRESH_FULL_SCREEN_ACTION = "com.anjie.lift.refresh_full_screen_action";

    /**
     * 构造
     */
    public BroadcastCenter()
    {

    }

    /**
     * 广播播放列表发生变化
     */
    public void notifyPlaylistChange()
    {
        Context context = AppContext.getInstance().getContext();
        Intent intent = new Intent();
        intent.setAction(PLAYLIST_CHANGE_ACTION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * 广播播放列表发生变化
     */
    public void cloudPlaylistChange()
    {
        Context context = AppContext.getInstance().getContext();
        Intent intent = new Intent();
        intent.setAction(PLAYLIST_CHANGE_ACTION_CLOUD);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    /**
     * 发送广播通知视图布局发生变化
     */
    public void notifyViewChange()
    {
        Context context = AppContext.getInstance().getContext();
        Intent intent = new Intent();
        intent.setAction(VIEW_CHANGE_ACTION);
        intent.setPackage(context.getPackageName());
        // 内部广播
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * 发送广播通知视图布局发生变化
     */
    public void notifyViewChangeLocal()
    {
        Context context = AppContext.getInstance().getContext();
        Intent intent = new Intent();
        intent.setAction(VIEW_CHANGE_ACTION_LOCAL);
        intent.setPackage(context.getPackageName());
        // 内部广播
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * 发送内部广播,通知控制中心，开始同步USB数据
     */
    public void notifyUSBSyncDataBegin()
    {
        Context context = AppContext.getInstance().getContext();
        Intent intent = new Intent();
        intent.setAction(USB_SYNC_DATA_ACTION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * USB同步数据结束
     */
    public void notifyUSBSyncDataFinish()
    {
        Context context = AppContext.getInstance().getContext();
        Intent intent = new Intent();
        intent.setAction(USB_SYNC_DATA_FINISH_ACTION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * 刷新全屏
     */
    public void refreshFullScreen()
    {
        Context context = AppContext.getInstance().getContext();
        Intent intent = new Intent();
        intent.setAction(REFRESH_FULL_SCREEN_ACTION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
