package com.anjie.common.threadpool;

public interface Future<T>
{
    public T get();
}
