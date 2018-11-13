package com.anjie.lift.server.request;

/**
 * 心跳请求
 */
public class HeartBeatRequest extends BaseRequest
{
    /**
     * 请求服务器的URL地址
     */
    private static final String URL_PATH = "";

    /**
     * 心跳请求
     *
     * @param isPost
     */
    public HeartBeatRequest(boolean isPost)
    {
        super(isPost, URL_PATH);
    }

    @Override
    protected String getPostData()
    {
        return null;
    }

    @Override
    protected String getGetParams()
    {
        return null;
    }

}
