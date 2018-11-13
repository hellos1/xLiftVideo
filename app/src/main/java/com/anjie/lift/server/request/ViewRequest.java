package com.anjie.lift.server.request;

/**
 * 主题布局请求视图
 */
public class ViewRequest extends BaseRequest
{
    /**
     * 请求类型
     */
    private static final String URL_PATH = "view";

    public ViewRequest(boolean isPost)
    {
        super(isPost, URL_PATH);
    }

    @Override
    protected String getGetParams()
    {
        StringBuffer builder = new StringBuffer();
        builder.append("pid=").append(getPid());
        builder.append("&did=").append(getDid());
        return builder.toString();
    }

    @Override
    protected String getPostData()
    {
        return null;
    }

}
