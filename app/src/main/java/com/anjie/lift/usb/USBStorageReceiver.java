package com.anjie.lift.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.anjie.common.log.LogX;

/**
 * USB存储设备监听器
 */
public class USBStorageReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction()))
        {
            // USB插入广播
            String usbFilePath = intent.getDataString();
            LogX.d("USBStorage","USB Device mounted. usbFilePath:" + usbFilePath);
            USBStorageManager.getInstance().scanUSBStorage(usbFilePath);
        }
        else if (Intent.ACTION_MEDIA_REMOVED.equals(intent.getAction()))
        {
            LogX.d("USBStorage", "USB Device removed.");
            // USB拔出广播
            USBStorageManager.getInstance().removeUSBStorage();
        }
    }
}
