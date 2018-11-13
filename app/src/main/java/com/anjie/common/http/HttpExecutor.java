package com.anjie.common.http;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.anjie.common.threadpool.Future;
import com.anjie.common.threadpool.FutureListener;
import com.anjie.common.threadpool.Job;
import com.anjie.common.threadpool.PriorityThreadFactory;
import com.anjie.common.threadpool.Worker;

/**
 * HTTP线程池类
 */
public final class HttpExecutor
{
    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 4;

    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = 8;

    /**
     * 线程生命时间 30秒
     */
    private static final int KEEP_ALIVE_TIME = 30;

    /**
     * 线程池
     */
    private final Executor mExecutor;

    /**
     * 单实例
     */
    private static HttpExecutor instance = null;

    /**
     * 私有构造
     */
    private HttpExecutor()
    {
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new PriorityThreadFactory("HttpThreadPool", android.os.Process.THREAD_PRIORITY_BACKGROUND));
    }

    /**
     * 单实例
     * 
     * @return
     */
    public synchronized static HttpExecutor getInstance()
    {
        if (instance == null)
        {
            instance = new HttpExecutor();
        }
        return instance;
    }

    public <T> Future<T> submit(Job<T> job, FutureListener<T> listener)
    {
        Worker<T> w = new Worker<T>(job, listener);
        mExecutor.execute(w);
        return w;

    }

    public <T> Future<T> submit(Job<T> job)
    {
        Worker<T> w = new Worker<T>(job, null);
        mExecutor.execute(w);
        return w;

    }
}
