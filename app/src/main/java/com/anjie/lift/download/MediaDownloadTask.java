package com.anjie.lift.download;

import java.io.File;

import com.anjie.common.log.LogX;
import com.anjie.common.util.Util;
import com.anjie.lift.app.AppInfoManager;
import com.anjie.lift.app.FileManager;

import android.text.TextUtils;

/**
 * 媒体下载任务信息
 */
public class MediaDownloadTask
{
    /**
     * 日志标签
     */
    private static final String TAG = "DownloadTask";

    /**
     * 服务器下发的路径
     */
    private String sPath;

    /**
     * 服务器下发的文件名
     */
    private String sFileName;

    /**
     * 媒体类型(图片还是视频)
     */
    private MediaType mType;

    /**
     * 下载的URL地址
     */
    private String downloadUrl;

    /**
     * 失败次数
     */
    private int downloadFailedTimes;

    /**
     * 最大重试次数
     */
    private final int MAX_RETRY_TIMES = 2;

    /**
     * 当前任务下载状态
     */
    private DownloadStatus taskStatus = DownloadStatus.Failed;

    /**
     * 下载状态
     */
    public enum DownloadStatus
    {
        Failed, Downloading, Success;

        public String toString()
        {
            return this.name();
        };
    }

    /**
     * 媒体类型
     */
    private enum MediaType
    {
        Video, Image
    }

    /**
     * 需求下发的path是唯一的
     * 
     * @param path
     *            下载的路径
     * @param name
     *            下载的文件名
     * @param type
     */
    public MediaDownloadTask(String path, String name, String type)
    {
        this.sPath = path;
        this.sFileName = buildFileName(path, name);
        this.mType = validType(type);
        this.downloadUrl = buildFullDownloadUrl(path);
    }

    /**
     * 获取保存文件的目录
     * 
     * @return
     */
    public String getSaveFileDir()
    {
        if (mType == MediaType.Image)
        {
            return FileManager.getInstance().getImagePathDir();
        }
        else if (mType == MediaType.Video)
        {
            return FileManager.getInstance().getVideoPathDir();
        }
        return null;
    }

    /**
     * 校验任务是否合法
     * 
     * @return
     */
    public boolean isValidTask()
    {
        if (TextUtils.isEmpty(sPath))
        {
            LogX.d(TAG, "sPath is empty.");
            return false;
        }
        if (TextUtils.isEmpty(sFileName))
        {
            LogX.d(TAG, "sName is empty.");
            return false;
        }
        if (mType == null)
        {
            LogX.d(TAG, "mType is null.");
            return false;
        }
        return true;
    }

    /**
     * 校验文件名
     * 
     * @param path
     * @param name
     * @return
     */
    private String buildFileName(String path, String name)
    {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(name))
        {
            return null;
        }
        return Util.getMD5Str(path) + "_" + name;
    }

    /**
     * 构建完整的下载URL地址
     * 
     * @param sPath
     * @return
     */
    private String buildFullDownloadUrl(String sPath)
    {
        String downloadHost = AppInfoManager.getInstance().getDownloadHost();
        if (TextUtils.isEmpty(downloadHost) || TextUtils.isEmpty(sPath))
        {
            return null;
        }

        if (downloadHost.endsWith("/"))
        {
            downloadHost = downloadHost.substring(0, downloadHost.length() - 1);
        }
        return downloadHost + sPath;
    }

    /**
     * 验证媒体类型
     * 
     * @param type
     * @return
     */
    private MediaType validType(String type)
    {
        if (TextUtils.isEmpty(type))
        {
            return null;
        }
        if ("1".equals(type.trim()))
        {
            return MediaType.Image;
        }
        else if ("0".equals(type.trim()))
        {
            return MediaType.Video;
        }
        return null;
    }

    /**
     * 累计错误次数
     */
    public void countTimes()
    {
        downloadFailedTimes++;
    }

    /**
     * 是否需要重试
     * 
     * @return
     */
    public boolean isNeedTry()
    {
        if (downloadFailedTimes <= MAX_RETRY_TIMES)
        {
            return true;
        }
        return false;
    }

    /**
     * 是否成功
     * 
     * @return
     */
    public boolean isSuccess()
    {
        return taskStatus == DownloadStatus.Success;
    }

    /**
     * 任务失败
     * 
     * @return
     */
    public boolean isFailed()
    {
        return taskStatus == DownloadStatus.Failed;
    }

    /**
     * 任务下载中
     * 
     * @return
     */
    public boolean isDownloading()
    {
        return taskStatus == DownloadStatus.Downloading;
    }

    /**
     * 获取失败次数
     * 
     * @return
     */
    public int getFailedTimes()
    {
        return downloadFailedTimes;
    }

    /**
     * 获取文件名
     * 
     * @return
     */
    public String getFileName()
    {
        return sFileName;
    }

    /**
     * 获取下载URL地址
     * 
     * @return
     */
    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    /**
     * 重置失败次数
     */
    public void resetFailedTimes()
    {
        downloadFailedTimes = 0;
    }

    /**
     * 获取下载任务状态
     * 
     * @return
     */
    public DownloadStatus getTaskStatus()
    {
        return taskStatus;
    }

    /**
     * 设置下载任务状态
     * 
     * @param taskStatus
     */
    public void setTaskStatus(DownloadStatus taskStatus)
    {
        this.taskStatus = taskStatus;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final MediaDownloadTask other = (MediaDownloadTask) obj;
        if (sPath == null)
        {
            return false;
        }
        if (sPath.equals(other.sPath))
        {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        if (TextUtils.isEmpty(sPath))
        {
            return 0;
        }
        return sPath.hashCode();
    }

    /**
     * 转换为播放列表Item
     * 
     * @return
     */
    public String toPlayListItem()
    {
        if (TextUtils.isEmpty(sFileName))
        {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (mType == MediaType.Image)
        {
            builder.append("<item type=\"image\">");
            builder.append("<path>").append(FileManager.IMAGE_DIR + File.separator + sFileName).append("</path>");
            builder.append("</item>");
        }
        else if (mType == MediaType.Video)
        {
            builder.append("<item type=\"video\">");
            builder.append("<path>").append(FileManager.VIDEO_DIR + File.separator + sFileName).append("</path>");
            builder.append("</item>");
        }
        return builder.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[url:").append(downloadUrl);
        builder.append(",downloadFailedTimes:").append(downloadFailedTimes);
        builder.append(",sFileName:").append(sFileName).append("]");
        return builder.toString();
    }
}
