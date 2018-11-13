package com.anjie.lift.usb.info;

public class KoneMediaInfo
{
    /**
     * 媒体类型
     */
    private MediaType type;

    /**
     * 媒体路径
     */
    private String mediaPath;

    /**
     * 媒体类型
     */
    public enum MediaType
    {
        Picture, Video, Backgroud;
    }

    public KoneMediaInfo(MediaType type, String mediaPath)
    {
        this.type = type;
        this.mediaPath = mediaPath;
    }

    public MediaType getType()
    {
        return type;
    }

    public String getMediaPath()
    {
        return mediaPath;
    }

    @Override
    public String toString()
    {
        return "[type:" + type + ",Path:" + mediaPath + "]";
    }
}
