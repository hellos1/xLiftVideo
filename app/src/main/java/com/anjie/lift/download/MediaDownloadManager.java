package com.anjie.lift.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import com.anjie.common.http.DownloadCallback;
import com.anjie.common.http.DownloadExecutor;
import com.anjie.common.http.HttpDownload;
import com.anjie.common.http.HttpExecutor;
import com.anjie.common.log.LogX;
import com.anjie.common.storage.FileCacheService;
import com.anjie.common.threadpool.Future;
import com.anjie.common.threadpool.FutureListener;
import com.anjie.lift.app.AppPreference;
import com.anjie.lift.app.BroadcastCenter;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.download.MediaDownloadTask.DownloadStatus;
import com.anjie.lift.manager.MPlayerManager;
import com.anjie.lift.parse.PlayerListParser;
import com.anjie.lift.player.PlayerElement;
import com.anjie.lift.server.request.PlayListRequest;
import com.anjie.lift.server.response.PlayListResponse;

import android.content.Context;
import android.text.TextUtils;

/**
 * 媒体下载管理类
 * 
 */
public class MediaDownloadManager
{
    /**
     * 日志标签
     */
    private static final String TAG = "MediaDownloadManager";

    /**
     * 等待下载列表
     */
    private Set<MediaDownloadTask> downloadTaskList = new CopyOnWriteArraySet<MediaDownloadTask>();

    /**
     * 上下文
     */
    private Context mContext;

    /**
     * 播放列表有效期 24小时
     */
    private static final long PlayListExipre = 24 * 60 * 60;

    /**
     * 存储上一次服务器下发的报文资源
     */
    private static final String MEDIA_RES_SP_KEY = "media_res_sp_key";

    /**
     * 是否正在请求服务器播放列表资源
     */
    public static AtomicBoolean isReqServer = new AtomicBoolean(false);

    /**
     * 是否正在执行下载任务
     */
    private AtomicBoolean isDownloading = new AtomicBoolean(false);

    /**
     * 当前正在下载任务
     */
    private DownloadExecutor downloadExecutor = new DownloadExecutor();

    /**
     * 下载管理类
     * 
     * @param context
     */
    public MediaDownloadManager(Context context)
    {
        this.mContext = context;
        // 加载本地资源到内存
        initLocalRes();
    }

    /**
     * 初始化本地资源
     */
    private void initLocalRes()
    {
        String xmlRes = new AppPreference(mContext).getString(MEDIA_RES_SP_KEY, null);
        PlayListResponse resp = PlayerListParser.parserServerPlayList(xmlRes);
        // 将上一次从服务器获取的下载列表加载到内存中
        if (resp != null && resp.getMediaDownList() != null)
        {
            File file = null;
            for (MediaDownloadTask task : resp.getMediaDownList())
            {
                // 验证下载状态是否已经下载
                if (task != null)
                {
                    file = new File(task.getSaveFileDir(), task.getFileName());
                    if (file.exists())
                    {
                        task.setTaskStatus(DownloadStatus.Success);
                    }
                    downloadTaskList.add(task);
                }
            }
        }
    }

    /**
     * 请求服务器播放媒体信息
     */
    public synchronized void requestServerMediaInfo()
    {
        if (isReqServer.get())
        {
            // 如果正在请求服务器操作,则返回
            LogX.w(TAG, "is requesting from the server media. return;");
            return;
        }

        // AppPreference appSp = new AppPreference(mContext);
        // // Liuchun 暂时不要这个
        // boolean isNeedRequest = appSp.validPlayListExpire(PlayListExipre);
        // LogX.d(TAG, "is Need to request PlayList Resource:" + isNeedRequest);
        // if (!isNeedRequest)
        // {
        // // 有效期内,不需要请求,检查是否有失败的任务
        // validDownloadFailed();
        // return;
        // }
        // 设置状态,正在请求
        isReqServer.set(true);

        PlayListRequest request = new PlayListRequest(true);
        HttpExecutor.getInstance().submit(request, new FutureListener<String>()
        {
            @Override
            public void onFutureDone(Future<String> future)
            {
                if (future != null)
                {
                    handleServerResponse(future.get());
                }
                else
                {
                    LogX.d(TAG, "Request Server playList failed");
                }
                // 请求服务器结束
                isReqServer.set(false);
            }
        });
    }

    /**
     * 处理服务器返回的数据
     * 
     * @param jsonData
     */
    private void handleServerResponse(String jsonData)
    {
        PlayListResponse resp = PlayerListParser.parserServerPlayList(jsonData);
        LogX.d(TAG, "jsonData:" + jsonData);
        if (resp != null && resp.isRespSuccess())
        {
            // 更新播放列表请求时间
            AppPreference sp = new AppPreference(mContext);
            // sp.updatePlayListExpire();
            // 将成功的媒体资源缓存到SP中
            sp.putString(MEDIA_RES_SP_KEY, jsonData);
            buildDownloadTask(resp.getMediaDownList());
        }
        else
        {
            LogX.d(TAG, "Handle Sever response Play list Failed.");
        }
    }

    /**
     * 检查是否有下载失败的任务
     */
    private void validDownloadFailed()
    {
        if (isDownloading.get() || downloadTaskList.size() <= 0)
        {
            // 1.下载的任务还在继续, 2.没有下载任务
            return;
        }

        int failedTask = 0;
        for (MediaDownloadTask task : downloadTaskList)
        {
            if (task.getTaskStatus() == DownloadStatus.Failed)
            {
                task.resetFailedTimes();
                failedTask++;
            }
        }

        // 有失败的任务,继续下载失败的任务
        if (failedTask > 0)
        {
            // 继续下一个下载任务
            handleNextDownloadTask();
        }
        else
        {
            LogX.d(TAG, "validDownloadFailed: it has no failed download task.");
        }
    }

    /**
     * 建立下载任务
     * 
     * @param mList
     */
    private void buildDownloadTask(List<MediaDownloadTask> mList)
    {
        if (mList == null || mList.size() == 0)
        {
            return;
        }
        // 1. 检查本地是否有需要删除的文件
        // List<PlayerElement> mPlayList =
        // MPlayerManager.getInstance().getPlayList();
        // for()
        // {
        //
        // }

        List<MediaDownloadTask> mDeleteList = new ArrayList<MediaDownloadTask>();
        mDeleteList.addAll(downloadTaskList);
        for (MediaDownloadTask task : mList)
        {
            // 遍历服务器下发的任务,减少的就是需要删除的
            mDeleteList.remove(task);
        }

        // 2.遍历服务器下发列表,是否有新增的
        for (MediaDownloadTask task : mList)
        {
            if (!downloadTaskList.contains(task))
            {
                // 添加服务器新增的列表
                downloadTaskList.add(task);
            }
        }

        // 3.检查是否有删除的任务
        if (mDeleteList.size() > 0)
        {
            // 表示有删除的任务
            if (isDownloading.get())
            {
                // 停止所有下载任务
                downloadExecutor.cancelAllTask();
            }
            // 从主列表中删除(服务器服务器中已经删除的列表项)
            downloadTaskList.removeAll(mDeleteList);
        }
        else
        {
            LogX.d(TAG, "Sync from server playlist,no local file to delete.");
        }

        // 5.检查是否有下载任务
        LogX.d(TAG, "downloadTaskList.size():" + downloadTaskList.size());
        int isNeedDownloadNum = 0;
        for (MediaDownloadTask task : downloadTaskList)
        {
            if (task.getTaskStatus() != DownloadStatus.Success)
            {
                isNeedDownloadNum++;
            }
        }

        // 有下载任务,执行下载任务
        if (isNeedDownloadNum > 0)
        {
            if (!isDownloading.get())
            {
                // 如果当前不在下载,则开始检查下载任务
                handleNextDownloadTask();
            }
        }
        else
        {
            if (mDeleteList.size() > 0)
            {
                finishAllTask();
                resetDownloadFailedTask();
                LogX.d(TAG, "Sync from server playlist,it has delete item rebuild playlist.xml.");
            }
            else
            {
                LogX.d(TAG, "Sync from server playlist,no new file to download.");
            }
        }
    }

    /**
     * 建立下载任务
     * 
     * @param task
     */
    private void excuteDownloadTask(final MediaDownloadTask task)
    {
        LogX.d(TAG, "excuteDownloadTask:" + task);
        isDownloading.set(true);
        HttpDownload download = new HttpDownload(task.getDownloadUrl(), task.getSaveFileDir(), task.getFileName(), new DownloadCallback()
        {
            @Override
            public void onSuccess()
            {
                LogX.d(TAG, "Download onSuccess");
                // 处理下载成功任务
                handleDownloadSuccess(task);
            }

            @Override
            public void onError(int code, String message)
            {
                LogX.e(TAG, "Download failed:" + code + ",message:" + message);
                // 处理下载失败的任务
                handleDownloadFailed(task);
            }
        }, true);
        task.countTimes();
        task.setTaskStatus(DownloadStatus.Downloading);
        // 提交给下载任务
        downloadExecutor.submit(download);
    }

    /**
     * 处理下载成功
     * 
     * @param task
     */
    private void handleDownloadSuccess(MediaDownloadTask task)
    {
        task.setTaskStatus(DownloadStatus.Success);
        // 处理下一个任务
        handleNextDownloadTask();
    }

    /**
     * 处理下载失败
     * 
     * @param task
     */
    private void handleDownloadFailed(MediaDownloadTask task)
    {
        task.setTaskStatus(DownloadStatus.Failed);
        // 处理下一个下载任务
        handleNextDownloadTask();
    }

    /**
     * 处理下一个下载任务
     */
    private synchronized void handleNextDownloadTask()
    {
        // 1.检查下载失败队列需要重试的任务
        MediaDownloadTask t = getNeedRetryTask();
        LogX.d(TAG, "getNeedRetryTask:" + t);
        if (t != null)
        {
            excuteDownloadTask(t);
            return;
        }
        // 2.如果失败队列没有重试的任务,结束本次下载,广播通知
        finishAllTask();
        resetDownloadFailedTask();
        isDownloading.set(false);
    }

    /**
     * 重置下载失败的任务
     */
    private void resetDownloadFailedTask()
    {
        for (MediaDownloadTask task : downloadTaskList)
        {
            if (task != null)
            {
                task.resetFailedTimes();
            }
        }
    }

    /**
     * 是否有需要重试的下载任务
     * 
     * @return
     */
    private MediaDownloadTask getNeedRetryTask()
    {
        MediaDownloadTask task = null;
        for (MediaDownloadTask t : downloadTaskList)
        {
            if (t.getTaskStatus() == DownloadStatus.Failed && t.isNeedTry())
            {
                task = t;
                break;
            }
        }
        return task;
    }

    /**
     * 结束所有下载任务
     */
    private void finishAllTask()
    {
        if (downloadTaskList.size() <= 0)
        {
            return;
        }
        int playItemNum = 0;
        // 构建播放列表内容
        StringBuilder playListBuilder = new StringBuilder();
        playListBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        playListBuilder.append("<playlist>");
        String item = null;
        for (MediaDownloadTask task : downloadTaskList)
        {
            if (task.isSuccess())
            {
                item = task.toPlayListItem();
                LogX.d(TAG, "build playlist item:" + item);
                if (!TextUtils.isEmpty(item))
                {
                    playItemNum++;
                    playListBuilder.append(item);
                }
            }
        }
        playListBuilder.append("</playlist>");

        // 生成的播放列表数
        if (playItemNum > 0)
        {
            String playListPath = FileManager.getInstance().getPlayListPath();
            // 写文件生成playlist.xml
            boolean bResult = FileCacheService.writeFile(playListPath, playListBuilder.toString());
            if (bResult)
            {
                new BroadcastCenter().notifyPlaylistChange();
            }
        }
    }
}
