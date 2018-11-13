package com.anjie.common.threadpool;

import com.anjie.common.log.LogX;

public final class Worker<T> implements Runnable, Future<T>
{
    /**
     * 任务
     */
    private Job<T> mJob;

    /**
     * 监听器
     */
    private FutureListener<T> mListener;

    /**
     * 执行结果
     */
    private T mResult;

    /**
     * 工作任务构造
     * 
     * @param job
     * @param listener
     */
    public Worker(Job<T> job, FutureListener<T> listener)
    {
        this.mJob = job;
        this.mListener = listener;
    }

    @Override
    public T get()
    {
        return mResult;
    }

    @Override
    public void run()
    {
        try
        {
            mResult = mJob.run();
        }
        catch (Throwable ex)
        {
            LogX.e("", "Worker execute meet Throwable!", ex);
        }

        if (mListener != null)
        {
            mListener.onFutureDone(this);
        }
    }
}
