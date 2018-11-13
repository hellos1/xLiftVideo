package com.anjie.common.threadpool;


public interface Job<T>
{
    public T run();
}
