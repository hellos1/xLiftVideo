package com.anjie.lift.usb.info;

import android.text.TextUtils;

/**
 * 隐藏区域
 */
public final class HiddenArea
{
    /**
     * 区域类型
     */
    public enum AreaType
    {
        Timer, Title, ScrollText;
    }

    /**
     * 区域类型
     */
    private AreaType type;

    /**
     * 显示值
     */
    private String visibleValue;

    /**
     * 隐藏区域
     * 
     * @param type
     *            区域类型
     * @param visibleValue
     */
    public HiddenArea(AreaType type, String visibleValue)
    {
        this.type = type;
        this.visibleValue = visibleValue;
    }

    /**
     * 是否隐藏
     * 
     * @return
     */
    public boolean isHidden()
    {
        if (TextUtils.isEmpty(visibleValue))
        {
            return false;
        }
        if ("false".equalsIgnoreCase(visibleValue))
        {
            return false;
        }
        return true;
    }

    /**
     * 获取区域类型
     * 
     * @return
     */
    public AreaType getAreaType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return "[type:" + type + ",value:" + visibleValue + "]";
    }
}
