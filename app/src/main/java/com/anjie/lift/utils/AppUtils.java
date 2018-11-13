package com.anjie.lift.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.anjie.common.log.LogX;
import com.anjie.lift.app.AppContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * APP辅助工具类
 */
public final class AppUtils
{
    /**
     * 获取图片
     * 
     * @param imagePath
     * @return
     */
    public static Drawable getDrawable(String imagePath)
    {
        Drawable drawable = null;
        Bitmap bgImage = loadBitmap(imagePath);
        if (bgImage != null)
        {
            Context context = AppContext.getInstance().getContext();
            drawable = new BitmapDrawable(context.getResources(), bgImage);
        }
        return drawable;
    }

    /**
     * 获取图片
     * 
     * @param imagePath
     * @return
     */
    public static Bitmap loadBitmap(String imagePath)
    {
        if (TextUtils.isEmpty(imagePath))
        {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        return bitmap;
    }

    /**
     * 当前网络连接是否正常
     * 
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context)
    {
        NetworkInfo networkInfo = getNetworkInfo(context);
        if (networkInfo != null && networkInfo.isAvailable())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * 获取网络类型 TYPE_MOBILE(0), TYPE_WIFI(1), TYPE_WIMAX(6), TYPE_ETHERNET(9),
     * TYPE_BLUETOOTH(7)
     * 
     * @param context
     * @return
     */
    public static int getNetWorkType(Context context)
    {
        NetworkInfo networkInfo = getNetworkInfo(context);
        if (networkInfo != null)
        {
            return networkInfo.getType();
        }
        else
        {
            return -1;
        }
    }

    /**
     * 获取网络类型
     * 
     * @param context
     * @return
     */
    private static NetworkInfo getNetworkInfo(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    /**
     * 获取当前的版本号versionCode
     * 
     * @param context
     * @return
     */
    public static int getVersionCode(Context context)
    {
        int versionCode = 0;
        try
        {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            versionCode = info.versionCode;
        }
        catch (Exception e)
        {
            LogX.e("get Version name meet exception.", e);
        }
        return versionCode;
    }

    /**
     * 获取当前的版本名称versionName
     * 
     * @param context
     * @return
     */
    public static String getVersionName(Context context)
    {
        String version = null;
        try
        {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        }
        catch (Exception e)
        {
            LogX.e("get Version name meet exception.", e);
        }
        return version;
    }

    /**
     * 获取Mac的地址
     * 
     * @return 地址字符串
     */
    public static String getMacAddress()
    {
        String macSerial = null;
        String str = "";
        try
        {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/eth0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str;)
            {
                str = input.readLine();
                if (str != null)
                {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        }
        catch (IOException ex)
        {
            // 赋予默认值
            LogX.e("Get Mac Address meet exception.", ex);
        }
        return macSerial.toUpperCase();
    }
}
