package com.anjie.lift.server.response;

import java.util.ArrayList;
import java.util.List;

import com.anjie.lift.download.MediaDownloadTask;

/**
 * 播放列表相应类
 */
public class PlayListResponse extends ServerResponse
{

    /**
     * 返回播放下载列表
     */
    private List<MediaDownloadTask> mDownloadList = new ArrayList<MediaDownloadTask>();

    /**
     * 播放列表相应请求
     */
    public PlayListResponse()
    {

    }

    /**
     * 添加要下载的任务
     * 
     * @param itemTask
     */
    public void addMediaDownloadTask(MediaDownloadTask itemTask)
    {
        mDownloadList.add(itemTask);
    }

    /**
     * 获取要下载的任务列表
     * 
     * @return
     */
    public List<MediaDownloadTask> getMediaDownList()
    {
        return mDownloadList;
    }
}
