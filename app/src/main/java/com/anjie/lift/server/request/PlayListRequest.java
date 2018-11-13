package com.anjie.lift.server.request;

import org.json.JSONException;
import org.json.JSONObject;

import com.anjie.common.log.LogX;

/**
 * 播放列表HTTP请求
 */
public final class PlayListRequest extends BaseRequest
{
    /**
     * 
     */
    private static final String TAG = "PlayListRequest";

    /**
     * 请求服务器的URL路径
     */
    private static final String URL_PATH = "/Admin/AdminPcontentManage/Getplaylist";

    /**
     * 播放列表请求构造函数
     * 
     * @param isPost
     *            是否Post方式请求
     */
    public PlayListRequest(boolean isPost)
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
        JSONObject obj = new JSONObject();
        try
        {
            obj.put("pid", getPid());
            obj.put("did", getDid());
        }
        catch (JSONException e)
        {
            LogX.e(TAG, "create playlist request json meet exception.", e);
        }
        return obj.toString();
    }

}
