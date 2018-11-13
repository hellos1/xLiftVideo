package com.anjie.lift.service.adapter.best;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.anjie.lift.app.AppContext;

/**
 * Created by xu on 2018/4/27.
 */

public class BestSpConfig {

    private static final String SP_NAME = "BestSpConfig";

    /**
     * bestSp
     */
    private SharedPreferences bestSp = null;

    /**
     * 单实例
     */
    private static BestSpConfig instance;

    /**
     * 私有构造
     */
    private BestSpConfig()
    {
        Context context = AppContext.getInstance().getContext();
        bestSp = context.getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE);
    }

    /**
     * 获取单实例
     */
    public static BestSpConfig getInstance(){
        if (instance == null)
        {
            instance = new BestSpConfig();
        }
        return instance;
    }



    //2018.04.26 SP_USED_DATE_FLOW，SP_DATA_FLOW_LIMIT，SP_SIZE_THRESHOLD
    /**
     * 服务器下发的  __usedDataFlow
     */
    private static final String SP_USED_DATE_FLOW = "used_DataFlow";

    /**
     * 服务器下发的  __dataFlowLimit
     */
    private static final String SP_DATA_FLOW_LIMIT = "data_FlowLimit";


    /**
     * 服务器下发的  __sizeThreshold
     */
    private static final String SP_SIZE_THRESHOLD = "size_Threshold";

    /**
     * 当前月份值
     */
    private static final String SP_MONTH = "save_month";


    private static final String SP_LAST_FLOW = "last_flow";

    /**
     * 初始化播放器
     */
    private static final String SP_INIT_VIDEO = "init_video";


    /**
     * 服务器下发的流量统计
     */
    public void setUsedDataFlow(long usedDataFlow)
    {
        bestSp.edit().putLong(SP_USED_DATE_FLOW, usedDataFlow).apply();
    }

    public long getUsedDataFlow()
    {
        return bestSp.getLong(SP_USED_DATE_FLOW, 0);
    }

    /**
     * 服务器下发的流量上限
     */
    public void setDataFlowLimit(long dataFlowLimit)
    {
        bestSp.edit().putLong(SP_DATA_FLOW_LIMIT, dataFlowLimit).apply();
    }

    public long getDataFlowLimit()
    {
        return bestSp.getLong(SP_DATA_FLOW_LIMIT, 150*1024);
    }

    /**
     * 服务器下发的流量阈值
     */
    public void setSizeThreshold(long sizeThreshold)
    {
        bestSp.edit().putLong(SP_SIZE_THRESHOLD, sizeThreshold).apply();
    }

    public long getSizeThreshold()
    {
        return bestSp.getLong(SP_SIZE_THRESHOLD, 20*1024);
    }

    public void setSaveMonth(int month)
    {
        bestSp.edit().putInt(SP_MONTH, month).apply();
    }

    public int getSaveMonth()
    {
        return bestSp.getInt(SP_MONTH, 0);
    }

    /**
     * 用于解决播放视频时的旋转问题
     */
    public boolean getIsInitVideo(){
        return bestSp.getBoolean(SP_INIT_VIDEO, false);
    }
    public void setIsInitVideo(boolean isInitVideo)
    {
        bestSp.edit().putBoolean(SP_INIT_VIDEO, isInitVideo).apply();
    }

}
