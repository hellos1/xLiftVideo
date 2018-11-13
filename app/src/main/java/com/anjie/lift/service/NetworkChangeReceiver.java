package com.anjie.lift.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.anjie.common.log.LogX;
import com.shbst.androiddevicesdk.DeviceSDK;
import com.shbst.androiddevicesdk.utils.NetworkUtils;

/**
 * 网络广播
 */
public class NetworkChangeReceiver extends BroadcastReceiver
{

    private final String TAG = "BestCloud";
    public NetworkChangeReceiver()
    {

    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
//        boolean isConnected = AppUtils.isNetworkConnected(context);
//        if (isConnected)
//        {
//            // 网络连接上,检测一次
//            Intent startIntent = new Intent();
//            startIntent.setClass(context, ResourceService.class);
//            startIntent.putExtra(ResourceService.COMMAND_KEY, ResourceService.COMMAND_NETWORK_CONNECTED);
//            context.startService(startIntent);
//        }

        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)
                || ConnectivityManager.CONNECTIVITY_ACTION.equals(action))
        {
            LogX.d(TAG, "receive action: " + action);
            if (NetworkUtils.isConnected(context))
            {
                if (NetworkUtils.isAvailableByPing(context))
                {
                    LogX.d(TAG, "onReceive: 网络可用");
                    DeviceSDK.getInstance().downloadRemainTasks();
                }
            }else {
                LogX.e(TAG, "onReceive: 网络不可用");
            }
        }
    }
}
