package com.anjie.lift.manager;

/**
 * 看门狗
 * 
 * Created by jimmy on 2017/6/23.
 */
public final class WatchDog
{
    private static final WatchDog instance = new WatchDog();

    /**
     * 私有构造
     */
    private WatchDog()
    {

    }

    /**
     * 获取单实例
     * 
     * @return
     */
    public static WatchDog getInstance()
    {
        return instance;
    }

    public void feedDog()
    {

    }
}
