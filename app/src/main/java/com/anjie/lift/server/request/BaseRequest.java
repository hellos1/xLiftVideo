package com.anjie.lift.server.request;

import java.util.HashMap;
import java.util.Map;

import com.anjie.common.http.HttpRequest;
import com.anjie.common.log.LogX;
import com.anjie.lift.app.AppInfoManager;

import android.text.TextUtils;

/**
 * 基本请求类
 */
public abstract class BaseRequest extends HttpRequest
{
    /**
     * 日志标签
     */
    private static final String TAG = "BaseRequest";

    public BaseRequest(boolean isPost, String urlPath)
    {
        super(getRequestUrl(urlPath), isPost);
        // 用户自定义的HTTP头信息
        Map<String, String> cusHeadParams = new HashMap<String, String>();
        cusHeadParams.put("njaj", "njaj120");
        cusHeadParams.put("Content-type", "application/json");
        // 用户可以自定义一些指定的HTTP请求头信息
        setHttpHeadParams(cusHeadParams);
    }

    /**
     * 拼接服务器请求地址
     * 
     * @param urlPath
     * @return
     */
    private static String getRequestUrl(String urlPath)
    {
        String serverHost = AppInfoManager.getInstance().getServerHost();
        if (TextUtils.isEmpty(serverHost))
        {
            LogX.w(TAG, "");
            return null;
        }
        // 兼容处理服务器地址末尾带/
        if (serverHost.endsWith("/"))
        {
            serverHost = serverHost.substring(0, serverHost.length() - 1);
        }
        // 返回请求服务器的完整URL路径
        return serverHost + urlPath;
    }

    /**
     * 获取PID
     * 
     * @return
     */
    protected String getPid()
    {
        return AppInfoManager.getInstance().getPid();
    }

    /**
     * 获取DID
     * 
     * @return
     */
    protected String getDid()
    {
        return AppInfoManager.getInstance().getDid();
    }
}
