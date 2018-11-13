package com.anjie.common.threadpool;

import java.util.concurrent.Executor;

public abstract class TaskExecutor
{
    /**
     * 线程池
     */
    protected Executor mExecutor;

    /**
     * 线程生命时间
     */
    protected static final int KEEP_ALIVE_TIME = 30;

    /**
     * 提交任务
     * 
     * @param job
     * @param listener
     * @return
     */
    public <T> Future<T> submit(Job<T> job, FutureListener<T> listener)
    {
        Worker<T> w = new Worker<T>(job, listener);
        mExecutor.execute(w);
        return w;
    }

    /**
     * 提交任务
     * 
     * @param job
     * @return
     */
    public <T> Future<T> submit(Job<T> job)
    {
        return submit(job, null);
    }
}
