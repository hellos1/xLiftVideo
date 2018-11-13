package com.anjie.lift.service.adapter;

public interface CloudAdapter
{
    void onCreate();

    void onSyncWithServer();

    void onDestroy();
}
