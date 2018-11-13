package com.anjie.lift.service.adapter.best;

/**
 * Best的云配置(服务器下发的报文字段)
 */
public abstract class BestCloudConfig
{
    /**
     * uid
     */
    public static final String uid = "uid";

    /**
     * 重置恢复出厂设置
     */
    public static final String reset = "reset";

    /**
     * 音量大小
     */
    public static final String volume = "volume";

    /**
     * 屏幕亮度
     */
    public static final String brightness = "brightness";

    /**
     * Standby brightness
     */
    public static final String sys_brightness = "sybrightness";

    /**
     * 全屏显示参数
     */
    public static final String full_screen = "fullscreen";

    /**
     * 屏保时间
     */
    public static final String screen_save_time = "screentime";

    /**
     * 隐藏标题参数
     */
    public static final String hidden_title = "title";

    /**
     * 隐藏滚动文字
     */
    public static final String hidden_scrollText = "scrolltext";

    /**
     * 隐藏时间
     */
    public static final String hidden_date = "date";

    /**
     * 图片显示时间
     */
    public static final String image_interval = "imageinterval";

    /**
     * 横竖屏的配置
     */
    public static final String display_type = "displaytype";

    /**
     *
     */
    public static final String time_format = "timemode";

    /**
     *
     */
    public static final String date_format = "datemode";


    //2018.04.26 以下是增加的字段 usedDataFlow，dataFlowLimit，sizeThreshold
    /**
     * 流量统计 单位:KB
     */
    public static final String usedDataFlow = "__usedDataFlow";

    /**
     * 流量上限 单位:KB
     */
    public static final String dataFlowLimit = "__dataFlowLimit";

    /**
     * 流量阈值 单位:KB
     */
    public static final String sizeThreshold = "__sizeThreshold";

}
