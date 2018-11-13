package com.anjie.common.log;

import android.util.Log;

/**
 * 日志模块
 */
public class LogX
{
    /**
     * Debug开关
     */
    private static boolean isDebug = true;

    /**
     * 程序标签
     */
    private static String APP_LOG_TAG = "LiftVideo";

    /**
     * Info级别日志
     * 
     * @param TAG
     * @param msg
     */
    public static void i(String TAG, String msg)
    {
        Log.i(APP_LOG_TAG, "[" + TAG + "]::" + msg + traceLog());
    }

    /**
     * Debug级别日志
     * 
     * @param msg
     */
    public static void d(String msg)
    {
        if (isDebug)
        {
            Log.d(APP_LOG_TAG, msg + traceLog());
        }
    }

    /**
     * Debug级别日志
     * 
     * @param TAG
     * @param msg
     */
    public static void d(String TAG, String msg)
    {
        if (isDebug)
        {
            Log.d(APP_LOG_TAG, "[" + TAG + "]::" + msg + traceLog());
        }
    }

    /**
     * Warning级别日志
     * 
     * @param TAG
     * @param msg
     */
    public static void w(String TAG, String msg)
    {
        Log.w(APP_LOG_TAG, "[" + TAG + "]::" + msg + traceLog());
    }

    public static void e(String msg)
    {
        if (isDebug)
        {
            Log.d(APP_LOG_TAG, msg + traceLog());
        }
    }

    public static void e(String TAG, String msg, Throwable e)
    {
        Log.e(APP_LOG_TAG, "[" + TAG + "]::" + msg + traceLog(), e);
    }

    public static void e(String msg, Throwable e)
    {
        Log.e(APP_LOG_TAG, msg + traceLog(), e);
    }

    public static void e(String TAG, String msg)
    {
        Log.e(APP_LOG_TAG, "[" + TAG + "]::" + msg + traceLog());
    }

    /**
     * 日志定位
     * 
     * @return
     */
    private static String traceLog()
    {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String tag = " [%s(Line:%d)]";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName
                .substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getLineNumber());
        return tag;
    }
}
