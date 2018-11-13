package com.anjie.common.threadpool;


public interface FutureListener<T>
{
	public void onFutureDone(Future<T> future);
}
