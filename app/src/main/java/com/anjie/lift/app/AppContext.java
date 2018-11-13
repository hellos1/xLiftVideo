package com.anjie.lift.app;

import android.content.Context;

public class AppContext
{
    private static AppContext instance;

    private Context mContext;

    public synchronized static AppContext getInstance()
    {
        if (instance == null)
        {
            instance = new AppContext();
        }
        return instance;
    }

    public void init(Context context)
    {
        this.mContext = context.getApplicationContext();
    }

    public Context getContext()
    {
        return mContext;
    }
}
