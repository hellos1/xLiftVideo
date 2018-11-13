package com.anjie.lift.download;

import java.util.concurrent.atomic.AtomicBoolean;

import com.anjie.common.http.DownloadCallback;
import com.anjie.common.http.HttpDownload;
import com.anjie.common.http.HttpExecutor;
import com.anjie.common.log.LogX;
import com.anjie.common.threadpool.Future;
import com.anjie.common.threadpool.FutureListener;
import com.anjie.lift.app.AppPreference;
import com.anjie.lift.app.BroadcastCenter;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.manager.ViewManager;
import com.anjie.lift.parse.ViewParser;
import com.anjie.lift.server.request.ViewRequest;
import com.anjie.lift.server.response.ViewResponse;

import android.content.Context;
import android.text.TextUtils;

/**
 * 视图下载管理类
 */
public class ViewDownloadManager
{
    /**
     * 日志标签
     */
    private static final String TAG = ViewDownloadManager.class.getSimpleName();

    /**
     * 请求服务器周期 24小时一次
     */
    private static final long ViewExpire = 24 * 60 * 60;

    /**
     * 上下文
     */
    private Context mContext = null;

    /**
     * 下载失败的URL
     */
    private static final String SP_DOWNLOAD_FAILED_URL = "sp_view_failed_url";

    /**
     * 是否正在请求服务器
     */
    private AtomicBoolean isReqServer = new AtomicBoolean(false);

    /**
     * 视图管理类
     * 
     * @param context
     */
    public ViewDownloadManager(Context context)
    {
        this.mContext = context;
    }

    /**
     * 请求服务器视图信息
     */
    public synchronized void requestServerViewInfo()
    {
        if (isReqServer.get())
        {
            return;
        }

        AppPreference sp = new AppPreference(mContext);
        boolean isNeedRequest = sp.validViewExpire(ViewExpire);
        if (!isNeedRequest)
        {
            // 时间戳不需要请求服务器,校验有没有失败的任务
            String failedUrl = sp.getString(SP_DOWNLOAD_FAILED_URL, "");
            validDownloadFailed(failedUrl);
            return;
        }
        // 开始请求服务器
        isReqServer.set(true);
        ViewRequest request = new ViewRequest(false);
        HttpExecutor.getInstance().submit(request, new FutureListener<String>()
        {
            @Override
            public void onFutureDone(Future<String> future)
            {
                if (future == null)
                {
                    LogX.d(TAG, "Request Server playList failed");
                    isReqServer.set(false);
                    return;
                }
                handleResponse(future.get());
            }
        });
    }

    /**
     * 处理服务器响应
     * 
     * @param xmlText
     */
    private void handleResponse(String xmlText)
    {
        ViewResponse resp = null;
        try
        {
            resp = ViewParser.parserServerView(xmlText);
        }
        catch (Exception e)
        {
            LogX.d(TAG, "Handle Sever response view meet exception.");
        }
        if (resp != null && resp.isRespSuccess())
        {
            // 更新播放列表请求时间
            new AppPreference(mContext).updateViewExpire();
            int cViewVersion = ViewManager.getInstance().getViewVersion();
            // 版本号递增
            if (resp.getViewVersion() > cViewVersion)
            {
                // 去下载服务器新的视图XML
                buildDownloadTask(resp.getDownloadUrl());
            }
            else
            {
                isReqServer.set(false);
            }
        }
        else
        {
            isReqServer.set(false);
            LogX.d(TAG, "Handle Sever response Play list Failed.");
        }
    }

    /**
     * 校验有没有失败的下载任务
     * 
     * @param failedUrl
     */
    private void validDownloadFailed(String failedUrl)
    {
        if (!TextUtils.isEmpty(failedUrl))
        {
            LogX.w(TAG, "It has failed view url:" + failedUrl);
            buildDownloadTask(failedUrl);
        }
    }

    /**
     * 解析成功的结果
     */
    private void buildDownloadTask(final String downloadUrl)
    {
        isReqServer.set(true);
        String filePath = FileManager.getInstance().getAppFileRoot();
        HttpDownload httpDownload = new HttpDownload(downloadUrl, filePath, FileManager.VIEW_FILE_NAME, new DownloadCallback()
        {
            @Override
            public void onSuccess()
            {
                new AppPreference(mContext).putString(SP_DOWNLOAD_FAILED_URL, "");
                new BroadcastCenter().notifyViewChange();
                // 任务结束
                isReqServer.set(false);
            }

            @Override
            public void onError(int code, String message)
            {
                isReqServer.set(false);
                new AppPreference(mContext).putString(SP_DOWNLOAD_FAILED_URL, downloadUrl);
            }
        }, false);
        HttpExecutor.getInstance().submit(httpDownload);
    }
}
