package com.anjie.common.threadpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class SingleTaskExecutor extends TaskExecutor
{
    /**
     * 单例
     */
    private static SingleTaskExecutor instance;

    /**
     * 私有构造
     */
    private SingleTaskExecutor()
    {
        mExecutor = new ThreadPoolExecutor(1, 1, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new PriorityThreadFactory("SingleTaskThread", android.os.Process.THREAD_PRIORITY_BACKGROUND));
    }

    /**
     * 获取单例
     * 
     * @return
     */
    public synchronized static SingleTaskExecutor getInstance()
    {
        if (instance == null)
        {
            instance = new SingleTaskExecutor();
        }
        return instance;
    }
}
