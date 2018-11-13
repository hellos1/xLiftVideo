package com.anjie.common.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.anjie.lift.app.AppContext;

/**
 * Created by jimmy on 2017/6/8.
 */

public class VersionInfo
{
    public VersionInfo()
    {

    }

    public int getVersionCode()
    {
        int currentVersionCode = 0;
        Context context = AppContext.getInstance().getContext();
        PackageManager manager = context.getPackageManager();
        try
        {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            currentVersionCode = info.versionCode; // 版本号
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return currentVersionCode;
    }
}
