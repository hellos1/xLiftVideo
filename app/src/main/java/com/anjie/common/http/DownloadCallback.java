package com.anjie.common.http;

/**
 * 请求回调函数
 */
public abstract class DownloadCallback
{
    /**
     * 下载失败回调函数
     * 
     * @param code
     * @param message
     */
    public abstract void onError(int code, String message);

    /**
     * 设置进度变化回调函数
     * 
     * @param currentLength
     * @param totalLength
     */
    public void onProgressChanged(long currentLength, long totalLength)
    {

    }

    /**
     * 取消回调接口
     */
    public void onCancel()
    {

    }

    /**
     * 成功回调通知接口
     */
    public abstract void onSuccess();
}
