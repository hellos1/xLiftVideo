package com.anjie.common.http;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;

import com.anjie.common.io.IOUtils;
import com.anjie.common.log.LogX;
import com.anjie.common.threadpool.Job;

import android.text.TextUtils;

/**
 * HTTP请求基类
 */
public abstract class HttpRequest implements Job<String>
{
    /**
     * 日志标签
     */
    private static final String TAG = "HttpRequest";

    /**
     * post请求定义
     */
    private static final int POST = 0;

    /**
     * get请求定义
     */
    private static final int GET = 1;

    /**
     * 数据缓冲区的大小
     */
    private static final int DATA_BUFFER_LEN = 512;

    /**
     * HTTP超时时间 20秒
     */
    private static final int HTTP_TIMEOUT = 20 * 1000;

    /**
     * HTTP读取IO数据超时时间
     */
    private static final int HTTP_READ_DATA_TIMEOUT = 5 * 1000;

    /**
     * 请求类型，默认为POST
     */
    private int requestType = POST;

    /**
     * URL地址
     */
    protected String httpUrl;

    /**
     * HTTP返回码
     */
    private int responseCode;

    /**
     * HTTP请求头参数
     */
    private Map<String, String> headParams = new HashMap<String, String>();

    /**
     * HTTP请求任务
     * 
     *
     * @param serverUrl
     * httpUrl
     * 连接地址
     * @param isPost
     * handler
     * 回调
     */
    protected HttpRequest(String serverUrl, boolean isPost)
    {
        this.httpUrl = serverUrl;
        this.requestType = isPost ? POST : GET;
    }

    /**
     * HTTP请求任务
     * 
     * @param isPost
     */
    protected HttpRequest(boolean isPost)
    {
        this.requestType = isPost ? POST : GET;
    }

    /**
     * 设置HTTP的报文头信息
     * 
     * @param headParams
     */
    protected void setHttpHeadParams(Map<String, String> headParams)
    {
        this.headParams = headParams;
    }

    /**
     * 设置服务器URL
     * 
     * @param serverUrl
     */
    protected void setServerUrl(String serverUrl)
    {
        this.httpUrl = serverUrl;
    }

    @Override
    public String run()
    {
        if (TextUtils.isEmpty(httpUrl))
        {
            LogX.w(TAG, "HttpRequest url can not be null.");
            return null;
        }
        return readData();
    }

    /**
     * 实现了联网写读功能
     * 
     * @throws Exception
     *             异常类
     * @throws Error
     *             错误类
     */
    private String readData()
    {
        HttpURLConnection connection = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        String respData = null;
        try
        {
            URL url = null;
            String getParam = getGetParams();
            if (requestType == GET && !TextUtils.isEmpty(getParam))
            {
                // 如果是Get方法,Get数据非空,携带URL数据
                url = new URL(httpUrl + "?" + getParam);
            }
            else
            {
                url = new URL(httpUrl);
            }
            // 构建连接
            connection = (HttpURLConnection) url.openConnection();
            // 不使用Cache
            connection.setUseCaches(false);
            // 设置连接主机超时（单位：毫秒）
            connection.setConnectTimeout(HTTP_TIMEOUT);
            // 设置从主机读取数据超时（单位：毫秒）
            connection.setReadTimeout(HTTP_READ_DATA_TIMEOUT);
            // 设置请求类型
            String requestMethod = requestType == POST ? "POST" : "GET";
            connection.setRequestMethod(requestMethod);
            LogX.d(TAG, "HTTP request type:" + requestMethod + ",url:" + url);
            byte[] requestData = null;
            String reqData = getPostData();
            if (!TextUtils.isEmpty(reqData))
            {
                LogX.d(TAG, "Post Data:" + reqData);
                requestData = reqData.getBytes("UTF-8");
            }
            // 以内容实体方式发送请求参数
            if (requestType == POST && requestData != null)
            {
                // 发送POST请求必须设置允许输出
                connection.setDoOutput(true);
                // 维持长连接
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Charset", "UTF-8");
                connection.setRequestProperty("Content-Length", String.valueOf(requestData.length));
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                attachHttpParams(connection);
                connection.connect();
                // 开始写入数据
                DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
                outStream.write(requestData);
                outStream.flush();
                outStream.close();
            }
            responseCode = connection.getResponseCode();
            LogX.i(TAG, "HTTP getResponseCode:" + responseCode);
            // 请求状态
            if (responseCode != HttpStatus.SC_OK)
            {
                return null;
            }
            // 读取数据
            // long fileLenght = (long) conn.getContentLength();
            // 获取网络数据输入流
            is = connection.getInputStream();
            bos = new ByteArrayOutputStream();
            // 读取的长度
            int length = 0;
            byte[] buf = new byte[DATA_BUFFER_LEN];
            while ((length = is.read(buf)) != -1)
            {
                // 循环读取数据直至为空
                bos.write(buf, 0, length);
            }
            byte[] data = bos.toByteArray();
            // 读取完后的文本数据
            respData = new String(data, HTTP.UTF_8);
            LogX.d(TAG, "HTTP response data:" + respData);
        }
        catch (Exception e)
        {
            LogX.e(TAG, "readData exception!", e);
        }
        finally
        {
            IOUtils.close(is);
            IOUtils.closeConnection(connection);
            IOUtils.close(bos);
        }
        return respData;
    }

    /**
     * 携带用户自定义的HTTP请求头信息
     */
    private void attachHttpParams(HttpURLConnection connection)
    {
        if (connection == null)
        {
            return;
        }
        if (headParams != null && headParams.size() > 0)
        {
            for (String key : headParams.keySet())
            {
                connection.setRequestProperty(key, headParams.get(key));
            }
        }
    }

    /**
     * 获取Post方式要提交的数据
     * 
     * @return POST方式数据
     */
    protected abstract String getPostData();

    /**
     * 获取Get方式要提交的数据
     * 
     * @return GET方式数据
     */
    protected abstract String getGetParams();
}
