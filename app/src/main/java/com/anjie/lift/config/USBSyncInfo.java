package com.anjie.lift.config;

/**
 * USB同步数据配置信息
 */
public final class USBSyncInfo
{
    /**
     * 图片和视频同步的类型
     */
    private int mediaSyncType;

    /**
     * 自定义字库路径
     */
    private String fontFileName;

    /**
     * 电梯上行图标资源
     */
    private String liftUpFileName;

    /**
     * 电梯下行图标资源
     */
    private String liftDownFileName;

    /**
     * 电梯抵达图标资源
     */
    private String liftArriveFileName;

    /**
     * 升级APK的
     */
    private String apkFileName;

    /**
     * 自定义的View
     */
    private String viewFileName;

    /**
     * USB资源同步信息
     */
    public USBSyncInfo()
    {

    }

    public int getMediaSyncType()
    {
        return mediaSyncType;
    }

    public void setMediaSyncType(int mediaSyncType)
    {
        this.mediaSyncType = mediaSyncType;
    }

    public String getFontFileName()
    {
        return fontFileName;
    }

    public void setFontFileName(String fontFileName)
    {
        this.fontFileName = fontFileName;
    }

    public String getLiftUpFileName()
    {
        return liftUpFileName;
    }

    public void setLiftUpFileName(String liftUpFileName)
    {
        this.liftUpFileName = liftUpFileName;
    }

    public String getLiftDownFileName()
    {
        return liftDownFileName;
    }

    public void setLiftDownFileName(String liftDownFileName)
    {
        this.liftDownFileName = liftDownFileName;
    }

    public String getLiftArriveFileName()
    {
        return liftArriveFileName;
    }

    public void setLiftArriveFileName(String liftArriveFileName)
    {
        this.liftArriveFileName = liftArriveFileName;
    }

    public String getApkFileName()
    {
        return apkFileName;
    }

    public void setApkFileName(String apkFileName)
    {
        this.apkFileName = apkFileName;
    }

    public String getViewFileName()
    {
        return viewFileName;
    }

    public void setViewFileName(String viewPath)
    {
        this.viewFileName = viewPath;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("{mediaSyncType:").append(mediaSyncType).append(",");
        buf.append("fontFileName:").append(fontFileName).append(",");
        buf.append("liftUpFileName:").append(liftUpFileName).append(",");
        buf.append("liftDownFileName:").append(liftDownFileName).append(",");
        buf.append("liftArriveFileName:").append(liftArriveFileName).append(",");
        buf.append("apkFileName:").append(apkFileName).append(",");
        buf.append("viewFileName:").append(viewFileName).append("}");
        return buf.toString();
    }
}
