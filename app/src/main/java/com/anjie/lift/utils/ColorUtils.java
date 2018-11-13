package com.anjie.lift.utils;

import com.anjie.common.log.LogX;

import android.graphics.Color;
import android.text.TextUtils;

/**
 * 颜色转换工具类
 */
public final class ColorUtils
{
    /**
     * 工具类
     */
    private static final String TAG = "ColorUtils";

    /**
     * RGB颜色转换
     *
     * @param rgbformat
     *            RGB颜色 123.125.155
     * @return Android 颜色
     */
    public static int rgbToColor(String rgbformat)
    {
        if (TextUtils.isEmpty(rgbformat))
        {
            return -1;
        }
        String[] arrays = rgbformat.split("\\.");
        if (arrays != null && arrays.length == 3)
        {
            int r = parseInt(arrays[0]);
            int g = parseInt(arrays[1]);
            int b = parseInt(arrays[2]);
            if (validRGB(r) && validRGB(g) && validRGB(b))
            {
                int color = Color.rgb(r, g, b);
                // LogX.d(TAG, "RGB(" + rgbformat + ") = r:" + r + ",g:" + g + ",b:" + b + " Android:color:" + color);
                return color;
            }
            else
            {
                return -1;
            }

        }
        else
        {
            return -1;
        }
    }

    /**
     * 校验RGB值
     *
     * @param value
     * @return
     */
    private static boolean validRGB(int value)
    {
        if (value >= 0 && value <= 255)
        {
            return true;
        }
        return false;
    }

    /**
     * int 类型转换
     *
     * @param strNum
     * @return
     */
    private static int parseInt(String strNum)
    {
        if (TextUtils.isEmpty(strNum))
        {
            return -1;
        }
        int num = -1;
        try
        {
            num = Integer.parseInt(strNum);
        }
        catch (NumberFormatException e)
        {
            LogX.e(TAG, "parseInt meet Exception.strNum:" + strNum, e);
        }
        return num;
    }
}
