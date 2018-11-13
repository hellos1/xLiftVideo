package com.anjie.lift.server.request;

/**
 * 版本请求
 */
public class VersionRequest extends BaseRequest
{
    /**
     * 版本请求的URL路径
     */
    private static final String URL_PATH = "/Admin/AdminDeviceManage/GetCurrentVersion";

    /**
     * 版本号请求
     * 
     * @param serverUrl
     * @param isPost
     */
    public VersionRequest(boolean isPost)
    {
        super(isPost, URL_PATH);
    }

    @Override
    protected String getGetParams()
    {
        StringBuffer builder = new StringBuffer();
        builder.append("pid=").append(getDid());
        builder.append("&did=").append(getDid());
        return builder.toString();
    }

    @Override
    protected String getPostData()
    {
        return null;
    }
}
