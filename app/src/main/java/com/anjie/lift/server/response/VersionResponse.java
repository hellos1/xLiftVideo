package com.anjie.lift.server.response;

public class VersionResponse extends ServerResponse
{
    /**
     * 版本号
     */
    private int versionCode;

    /**
     * 下载路径
     */
    private String path;

    /**
     * 版本信息返回类
     */
    public VersionResponse()
    {

    }

    public int getVersionCode()
    {
        return versionCode;
    }

    public void setVersionCode(int versionCode)
    {
        this.versionCode = versionCode;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
