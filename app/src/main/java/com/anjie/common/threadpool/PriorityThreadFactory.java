package com.anjie.common.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Process;

public final class PriorityThreadFactory implements ThreadFactory
{
    /**
     * 优先级
     */
    private final int priority;

    /**
     * 原子递增数
     */
    private final AtomicInteger number = new AtomicInteger();

    /**
     * 线程名
     */
    private final String threadName;

    /**
     * 构造函数
     * 
     * @param name
     * @param priority
     */
    public PriorityThreadFactory(String name, int priority)
    {
        this.threadName = name;
        this.priority = priority;
    }

    public Thread newThread(Runnable r)
    {
        return new Thread(r, threadName + '-' + number.getAndIncrement())
        {
            @Override
            public void run()
            {
                Process.setThreadPriority(priority);
                super.run();
            }
        };
    }

}
