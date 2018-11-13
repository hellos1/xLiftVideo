package com.anjie.common.util;

import com.anjie.common.log.LogX;

import android.text.TextUtils;

public class NumUtils
{
    /**
     * 日志标签
     */
    private static final String TAG = "NumUtils";

    /**
     * 安全转化数字
     * 
     * @param numStr
     * @param defResult
     * @return
     */
    public static int parseSafeInt(String numStr, int defResult)
    {
        int num = 0;
        if (TextUtils.isEmpty(numStr))
        {
            return defResult;
        }
        try
        {
            num = Integer.parseInt(numStr.trim());
        }
        catch (NumberFormatException e)
        {
            num = defResult;
            LogX.d("NumUtils", "parseInt meet NumberFormatException! numStr:" + numStr);
        }
        return num;
    }

    /**
     * 安全的整形转换
     * 
     * @param numStr
     * @param defResult
     * @return
     */
    public static long parseSafeLong(String numStr, long defResult)
    {
        long num = 0;
        try
        {
            num = Long.parseLong(numStr);
        }
        catch (NumberFormatException e)
        {
            num = defResult;
            LogX.d(TAG, "parseLong meet NumberFormatException! numStr:" + numStr);
        }
        return num;
    }

    public static long[] getBreakDownloadRange(String range)
    {
        return null;
    }
}
