package com.anjie.lift.service.adapter.best;

import android.net.Uri;
import android.text.TextUtils;

import com.anjie.common.io.IOUtils;
import com.anjie.common.log.LogX;
import com.anjie.common.storage.FileCacheService;
import com.anjie.lift.app.BroadcastCenter;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.manager.MPlayerManager;
import com.anjie.lift.parse.PlayerListParser;
import com.anjie.lift.player.PlayerElement;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.shbst.androiddevicesdk.DeviceSDK;
import com.shbst.androiddevicesdk.listener.FileDownloadAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Best下载管理类
 */
public class BestDownloadHelper
{
    /**
     * 日志标签
     */
    private static final String TAG = "BestCloud";

    /**
     * 单实例
     */
    private static final BestDownloadHelper instance = new BestDownloadHelper();

    /**
     * 是否正在下载
     */
    private boolean isDownloading = false;

    /**
     * 媒体资源下载列表
     */
    private Map<BestResItem, Status> mediaDownloadList = new ConcurrentHashMap<BestResItem, Status>();

    /**
     * 播放列表的Item
     */
    private List<String> playItemList = new ArrayList<String>();

    private enum Status
    {
        Failed, Downloading, Success;
    }

    /**
     * 私有构造函数
     */
    private BestDownloadHelper()
    {
        // 设置下载适配器
        DeviceSDK.getInstance().setFileDownloadAdapter(fileDownloadAdapter);
    }

    /**
     * 获取单实例
     * 
     * @return
     */
    public static BestDownloadHelper getInstance()
    {
        return instance;
    }

    /**
     * 下载监听器
     */
    private FileDownloadAdapter fileDownloadAdapter = new FileDownloadAdapter()
    {
        @Override
        public void pending(BaseDownloadTask baseDownloadTask, int soFarBytes,
                int totalBytes)
        {
            LogX.d(TAG,
                    "FileDownloadAdapter.pending:" + baseDownloadTask.getUrl());
        }

        @Override
        public void started(BaseDownloadTask baseDownloadTask)
        {
            LogX.d(TAG, "FileDownloadAdapter.started:" + baseDownloadTask);
        }

        @Override
        public void connected(BaseDownloadTask baseDownloadTask,
                boolean isContinue, int soFarBytes, int totalBytes)
        {
            LogX.d(TAG, "FileDownloadAdapter.connected:"
                    + baseDownloadTask.getFilename());
        }

        @Override
        public void progress(BaseDownloadTask baseDownloadTask, int soFarBytes,
                int totalBytes)
        {
            int percent = soFarBytes * 100 / totalBytes;
            LogX.d(TAG, "FileDownloadAdapter.progress:"
                    + baseDownloadTask.getFilename() + "," + percent + "%");
        }

        @Override
        public void blockComplete(BaseDownloadTask baseDownloadTask)
                throws Throwable
        {
            LogX.d(TAG, "FileDownloadAdapter.blockComplete:"
                    + baseDownloadTask.getFilename());
        }

        @Override
        public void retry(BaseDownloadTask baseDownloadTask,
                Throwable throwable, int retryingTimes, int soFarBytes)
        {
            LogX.d(TAG, "FileDownloadAdapter.retry:"
                    + baseDownloadTask.getFilename());
        }

        @Override
        public void completed(BaseDownloadTask baseDownloadTask)
        {
            LogX.d(TAG, "FileDownloadAdapter.completed:"
                    + baseDownloadTask.getFilename());
            checkDownloadList(baseDownloadTask, true);
        }

        @Override
        public void paused(BaseDownloadTask baseDownloadTask, int i, int i1)
        {
            LogX.d(TAG, "FileDownloadAdapter.completed:"
                    + baseDownloadTask.getFilename());
            checkDownloadList(baseDownloadTask, false);
        }

        @Override
        public void error(BaseDownloadTask baseDownloadTask,
                Throwable throwable)
        {
            LogX.d(TAG, "FileDownloadAdapter.error:"
                    + baseDownloadTask.getFilename());
            checkDownloadList(baseDownloadTask, false);
        }

        @Override
        public void warn(BaseDownloadTask baseDownloadTask)
        {
            LogX.d(TAG, "FileDownloadAdapter.warn:"
                    + baseDownloadTask.getFilename());
            checkDownloadList(baseDownloadTask, false);
        }
    };

    /**
     * 处理资源
     */
    public void handlePlayResource(List<BestResItem> list)
    {
        if (list == null || list.size() <= 0)
        {
            LogX.d(TAG, "handlePlayResource list is null or size = 0.");
            return;
        }
        LogX.d(TAG, "handlePlayResource size:" + list.size());
        String[] urlList = new String[list.size()];
        String[] savePathList = new String[list.size()];
        BestResItem item;
        for (int i = 0; i < list.size(); i++)
        {
            item = list.get(i);
            // 封装下载URL数组
            //2018.05.10 url编码
            urlList[i] = encodeUrl(item.getContent());

            //urlList[i] = item.getContent();
            // 封装保存路径数组
            switch (item.getType()){
                case Picture:
                    savePathList[i] = FileManager.getInstance().getImagePathDir();
                    break;
                case Video:
                    savePathList[i] = FileManager.getInstance().getVideoPathDir();
                    break;
                case Backgroud:
                    savePathList[i] = FileManager.getInstance().getAudioPathDir();
                    break;
                case audio:
                    savePathList[i] = FileManager.getInstance().getAudioPathDir();
                    break;
            }
        }

        if (isDownloading)
        {
            // 如果正在下载,停止之前的任务
            DeviceSDK.getInstance().stopDownload();
        }
        // 设置正在下载
        isDownloading = true;

        // TODO
        // 目前可以从Best的服务器下下载播放资源了。
        // mediaDownloadList目前用于内存存放要下载的资源
        // playItemList用于下载成功的文件记录.
        // Best根据URL进行MD5加密算法生成了文件名，且文件名没有后缀

        // 构建下载列表状态
        mediaDownloadList.clear();
        playItemList.clear();
        for (BestResItem resItem : list)
        {
            mediaDownloadList.put(resItem, Status.Downloading);
        }
        // 启动下载
        boolean bResult = DeviceSDK.getInstance().startDownload(urlList,
                savePathList);
        LogX.d(TAG, "startDownload result:" + bResult);
    }

    /**
     * 2018.05.10 下载url编码, 以适应有中文的url
     */
    private static String encodeUrl(String url) {
        return Uri.encode(url, "-![.:/,%?&=]");
    }

    /**
     * 检查下载列表任务
     * 
     * @param baseDownloadTask
     * @param bSuccess
     */
    private synchronized void checkDownloadList(
            BaseDownloadTask baseDownloadTask, boolean bSuccess)
    {
        String playItemPath;
        // 更新下载列表状态
        for (BestResItem item : mediaDownloadList.keySet())
        {
            if (encodeUrl(item.getContent()).equals(baseDownloadTask.getUrl()))
            {
                if (bSuccess)
                {
                    mediaDownloadList.put(item, Status.Success);
                    LogX.d(TAG, "item is:" + item.toString());
                    playItemPath = buildPlayItem(item,
                            baseDownloadTask.getFilename());
                    if (playItemPath != null)
                    {
                        playItemList.add(playItemPath);
                    }
                }
                else
                {
                    mediaDownloadList.put(item, Status.Failed);
                }
                break;
            }
        }
        // 检查是否所有的任务都结束了
        checkAllDone();
    }

    /**
     * 检查所有任务是否结束
     */
    private void checkAllDone()
    {
        boolean isAllFinish = true;
        for (BestResItem item : mediaDownloadList.keySet())
        {
            if (mediaDownloadList.get(item) == Status.Downloading)
            {
                // 如果还有下载中的状态
                isAllFinish = false;
                break;
            }
        }
        // 所有任务都结束了,就开始生成播放列表
        if (isAllFinish && playItemList.size() > 0)
        {
            String playListFile = FileManager.getInstance().getPlayListPath();
            //String content = buildPlayList(playItemList);
            String content = buildList(playItemList);
            // 生成播放列表
            boolean bResult = false;
            if (!TextUtils.isEmpty(content))
            {
                bResult = FileCacheService.writeFile(playListFile, content);
            }
            if (bResult)
            {
                // 通知播放列表发生变化
                new BroadcastCenter().cloudPlaylistChange();
                //休眠模式设置
                MPlayerManager.getInstance().bootPlay.getAndSet(true);
                //MPlayerManager.getInstance().setTagPTrue();
            }
        }
    }


    /**
     * 2018.05.04 列表构建
     */
    //2018.05.04
    private String buildList(List<String> mList)
    {
        //原先存在的playlist.xml
        InputStream inputStream = null;
        List<PlayerElement> newPlayList = null;
        try
        {
            String playListPath = FileManager.getInstance().getPlayListPath();
            File playListFile = new File(playListPath);
            if (playListFile.exists()) {
                inputStream = new FileInputStream(playListFile);

                // 解析出新的播放列表
                newPlayList = PlayerListParser.parse(inputStream);
            }

        }
        catch (Exception e)
        {
            LogX.e("BestCloud Loading parse PlayList meet exception!", e);
        }
        finally
        {
            // 关闭流
            IOUtils.close(inputStream);
        }
        boolean is_pic = false;
        boolean is_audio = false;
        boolean is_video = false;

        if (mList != null && mList.size() > 0)
        {
            StringBuffer buf = new StringBuffer();
            int num = 0;
            buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            buf.append("<playlist>");
            for (String item : mList)
            {
                if (!TextUtils.isEmpty(item))
                {
                    if (item.startsWith(FileManager.IMAGE_DIR))
                    {
                        buf.append("<item type=\"image\">");
                        buf.append("<path>").append(item).append("</path>");
                        buf.append("</item>");
                        num++;
                        is_pic = true;
                    }
                    else if (item.startsWith(FileManager.VIDEO_DIR))
                    {
                        buf.append("<item type=\"video\">");
                        buf.append("<path>").append(item).append("</path>");
                        buf.append("</item>");
                        num++;
                        is_video=true;
                    }
                    else if (item.startsWith(FileManager.AUDIO_DIR))
                    {
                        buf.append("<item type=\"audio\">");
                        buf.append("<path>").append(item).append("</path>");
                        buf.append("</item>");
                        num++;
                        is_audio=true;
                    }
                }
            }

            for(PlayerElement newitem: newPlayList)
            {
                if(newitem.getType()== PlayerElement.ElementType.audio)
                {
                    if(is_pic || is_video){//如果这次加的是图片或者视频，把以前的audio加进去
                        buf.append("<item type=\"audio\">");
                        buf.append("<path>").append(newitem.getFilePath()).append("</path>");
                        buf.append("</item>");
                        num++;
                    }

                }else  if(newitem.getType()== PlayerElement.ElementType.image)
                {
                    if(is_audio){//如果这次是音频，把以前的图片加进去
                        buf.append("<item type=\"image\">");
                        buf.append("<path>").append(newitem.getFilePath()).append("</path>");
                        buf.append("</item>");
                        num++;
                    }
                }
                else  if(newitem.getType()== PlayerElement.ElementType.video)
                {
                    if(is_audio) {//如果这次是音频，把以前的视频加进去
                        buf.append("<item type=\"video\">");
                        buf.append("<path>").append(newitem.getFilePath()).append("</path>");
                        buf.append("</item>");
                        num++;
                    }
                }
            }
            buf.append("</playlist>");
            // 返回播放列表内容
            return num > 0 ? buf.toString() : null;
        }
        return null;
    }


    /**
     * 构建播放列表的内容
     * 
     * @param mList
     *            播放列表的Item
     * @return 播放列表的xml内容
     */
    private String buildPlayList(List<String> mList)
    {
        if (mList != null && mList.size() > 0)
        {
            StringBuffer buf = new StringBuffer();
            int num = 0;
            buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            buf.append("<playlist>");
            for (String item : mList)
            {
                if (!TextUtils.isEmpty(item))
                {
                    if (item.startsWith(FileManager.IMAGE_DIR))
                    {
                        buf.append("<item type=\"image\">");
                        buf.append("<path>").append(item).append("</path>");
                        buf.append("</item>");
                        num++;
                    }
                    else if (item.startsWith(FileManager.VIDEO_DIR))
                    {
                        buf.append("<item type=\"video\">");
                        buf.append("<path>").append(item).append("</path>");
                        buf.append("</item>");
                        num++;
                    }
                    else if (item.startsWith(FileManager.AUDIO_DIR))// Liuchun
                    {
                        buf.append("<item type=\"audio\">");
                        buf.append("<path>").append(item).append("</path>");
                        buf.append("</item>");
                        num++;
                    }
                }
            }
            buf.append("</playlist>");
            // 返回播放列表内容
            return num > 0 ? buf.toString() : null;
        }
        return null;
    }

    /**
     * 构建播放列表选项
     * 
     * @param item
     *            资源Item
     * @param fileName
     *            文件名
     * @return
     */
    private String buildPlayItem(BestResItem item, String fileName)
    {
        if (item.getType() == BestResItem.ResourceType.Picture)
        {
            return "image" + File.separator + fileName;
        }
        else if (item.getType() == BestResItem.ResourceType.Video)
        {
            return "video" + File.separator + fileName;
        }
        else if (item.getType() == BestResItem.ResourceType.Backgroud)// Liuchun
        {
            return "audio" + File.separator + fileName;
        }
        // 2018.05.03 wangxu
        else if (item.getType() == BestResItem.ResourceType.audio)
        {
            return "audio" + File.separator + fileName;
        }
        return null;
    }

    /**
     * 处理APK下载任务
     */
    public void handleAPKDownload()
    {

    }

    /**
     * 处理WebView的resource资源
     */
    public void handleWebViewResource(String webUrl)
    {
        if (TextUtils.isEmpty(webUrl) || !webUrl.startsWith("http"))
        {
            return;
        }
        LogX.i(TAG, "handleWebViewResource:" + webUrl);
        StringBuffer buf = new StringBuffer();
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buf.append("<playlist>");
        buf.append("<item type=\"url\">");
        buf.append("<path>").append(webUrl).append("</path>");
        buf.append("</item>");
        buf.append("<playlist>");

        String playListFile = FileManager.getInstance().getPlayListPath();
        // 生成播放列表
        if (FileCacheService.writeFile(playListFile, buf.toString()))
        {
            LogX.i(TAG,"Write web url content playlist success,notify playlist change.");
            // 通知播放列表发生变化
            //new BroadcastCenter().notifyPlaylistChange();
            //2018.05.03
            new BroadcastCenter().cloudPlaylistChange();
        }
        else
        {
            LogX.w(TAG, "handleWebViewResource, write playlist file failed.");
        }
    }
}
