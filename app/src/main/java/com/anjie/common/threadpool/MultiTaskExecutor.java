package com.anjie.common.threadpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class MultiTaskExecutor extends TaskExecutor
{

    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 3;

    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = 6;

    /**
     * 单例
     */
    private static MultiTaskExecutor instance;

    /**
     * 私有构造
     */
    private MultiTaskExecutor()
    {
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new PriorityThreadFactory("MultiTaskThread", android.os.Process.THREAD_PRIORITY_BACKGROUND));
    }

    /**
     * 获取单实例
     * 
     * @return
     */
    public synchronized static MultiTaskExecutor getInstance()
    {
        if (instance == null)
        {
            instance = new MultiTaskExecutor();
        }
        return instance;
    }
}
