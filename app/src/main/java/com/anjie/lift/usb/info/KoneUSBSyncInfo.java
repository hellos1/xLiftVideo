package com.anjie.lift.usb.info;

import java.util.ArrayList;
import java.util.List;

import com.anjie.lift.config.SavePowerMode;

/**
 * 通力USB同步标准化信息
 */
public class KoneUSBSyncInfo
{
    /**
     * 恢复出厂设置模式
     */
    private boolean resetMode = false;

    /**
     * 标题
     */
    private String title;

    /**
     * 滚动文本
     */
    private String scrollText;

    /**
     * 声音大小
     */
    private int volume = -1;

    /**
     * 亮度大小
     */
    private int brightness = -1;

    /**
     * 多媒体路径
     */
    private List<KoneMediaInfo> mediaInfoList = new ArrayList<KoneMediaInfo>();

    /**
     * 一级节能模式
     */
    private SavePowerMode stageFirst;

    /**
     * 二级节能模式
     */
    private SavePowerMode stageSecond;

    /**
     * 全屏模式(-1不处理；0-非全屏，1-全屏)
     */
    private int fullScreenValue = -1;

    /**
     * 图片间隔时间
     */
    private int pictureInterval = -1;

    /**
     * 时间格式
     */
    private String timeFormat;

    /**
     * 日期格式
     */
    private String dateFormat;

    /**
     * 隐藏区域
     */
    private List<HiddenArea> hiddenAreaList = new ArrayList<HiddenArea>();

    /**
     * 构造函数
     */
    public KoneUSBSyncInfo()
    {

    }

    public boolean isResetMode()
    {
        return resetMode;
    }

    public void setResetMode(boolean resetMode)
    {
        this.resetMode = resetMode;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getScrollText()
    {
        return scrollText;
    }

    public void setScrollText(String scrollText)
    {
        this.scrollText = scrollText;
    }

    public int getVolume()
    {
        return volume;
    }

    public void setVolume(int volume)
    {
        this.volume = volume;
    }

    public int getBrightness()
    {
        return brightness;
    }

    public void setBrightness(int brightness)
    {
        this.brightness = brightness;
    }

    public List<KoneMediaInfo> getKoneMediaInfoList()
    {
        return mediaInfoList;
    }

    public void addKoneMediaInfo(KoneMediaInfo mediaInfo)
    {
        this.mediaInfoList.add(mediaInfo);
    }

    public SavePowerMode getStageFirst()
    {
        return stageFirst;
    }

    public void setStageFirst(SavePowerMode stageFirst)
    {
        this.stageFirst = stageFirst;
    }

    public SavePowerMode getStageSecond()
    {
        return stageSecond;
    }

    public void setStageSecond(SavePowerMode stageSecond)
    {
        this.stageSecond = stageSecond;
    }

    public int getFullScreenValue()
    {
        return fullScreenValue;
    }

    public void setFullScreenValue(int fullScreenValue)
    {
        this.fullScreenValue = fullScreenValue;
    }

    public List<HiddenArea> getHiddenAreaList()
    {
        return hiddenAreaList;
    }

    public void addHiddenArea(HiddenArea hiddenArea)
    {
        this.hiddenAreaList.add(hiddenArea);
    }

    public int getPictureInterval()
    {
        return pictureInterval;
    }

    public void setPictureInterval(int pictureInterval)
    {
        this.pictureInterval = pictureInterval;
    }

    public String getTimeFormat()
    {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat)
    {
        this.timeFormat = timeFormat;
    }

    public String getDateFormat()
    {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat)
    {
        this.dateFormat = dateFormat;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("{reset:").append(resetMode);
        buf.append(",title:").append(title);
        buf.append(",scrollText:").append(scrollText);
        buf.append(",volume:").append(volume);
        buf.append(",brightness:").append(brightness);
        buf.append(",fullScreenValue:").append(fullScreenValue);
        buf.append(",pictureInterval:").append(pictureInterval);
        buf.append(",timeFormat:").append(timeFormat);
        buf.append(",dateFormat:").append(dateFormat);
        buf.append(",mediaInfoList:").append(mediaInfoList);
        buf.append(",stageFirst:").append(stageFirst);
        buf.append(",stageSecond:").append(stageSecond);
        buf.append(",hiddenAreaList:").append(hiddenAreaList).append("}");
        return buf.toString();
    }
}
