package com.anjie.common.http;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.anjie.common.log.LogX;
import com.anjie.common.threadpool.PriorityThreadFactory;

/**
 * 下载执行器
 */
public final class DownloadExecutor
{

    /**
     * 日志标签
     */
    private static final String TAG = "DownloadExecutor";

    /**
     * 线程池
     */
    private final Executor mExecutor;

    /**
     * 任务列表容器
     */
    private List<DownloadRunnable> mTaskList = new CopyOnWriteArrayList<DownloadRunnable>();

    /**
     * 下载线程构造类
     */
    public DownloadExecutor()
    {
        // 使用单线程下载
        mExecutor = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new PriorityThreadFactory("HttpThreadPool", android.os.Process.THREAD_PRIORITY_BACKGROUND));
    }

    /**
     * 提交下载任务
     * 
     * @param download
     */
    public void submit(HttpDownload download)
    {
        DownloadRunnable task = new DownloadRunnable(download);
        mTaskList.add(task);
        mExecutor.execute(task);
    }

    /**
     * 取消所有下载任务
     */
    public void cancelAllTask()
    {
        if (mTaskList.size() <= 0)
        {
            return;
        }
        for (DownloadRunnable task : mTaskList)
        {
            task.cancelTask();
        }
    }

    /**
     * 现在线程任务
     */
    private class DownloadRunnable implements Runnable
    {
        /**
         * 下载任务
         */
        private HttpDownload downloadTask;

        /**
         * 下载线程任务
         * 
         * @param downloadTask
         *            下载任务
         */
        public DownloadRunnable(HttpDownload downloadTask)
        {
            this.downloadTask = downloadTask;
        }

        /**
         * 取消任务
         */
        void cancelTask()
        {
            if (downloadTask != null)
            {
                downloadTask.cancelTask();
            }
        }

        @Override
        public void run()
        {
            if (downloadTask != null)
            {
                try
                {
                    downloadTask.run();
                }
                catch (Throwable e)
                {
                    LogX.e(TAG, "Download task meet exception:" + e);
                }
                // 结束的任务移除掉
                mTaskList.remove(this);
            }
        }
    }
}
