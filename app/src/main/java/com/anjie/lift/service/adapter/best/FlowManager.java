package com.anjie.lift.service.adapter.best;

import android.net.TrafficStats;
import android.text.TextUtils;

import com.anjie.common.log.LogX;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Flow 流量
 * Created by xu on 2018/4/26.
 */

public class FlowManager {
    private static final String TAG = "FlowCounting";
    private static FlowManager instance;
    private static FlowTask flowTask;
    private FlowManager()
    {
        flowTask = new FlowTask();
    }

    public static FlowManager getInstance()
    {
        if (instance == null)
        {
            instance = new FlowManager();
        }
        return instance;
    }

    /**
     * 从时间戳中获取月份
     * @param timestamp
     * @return month
     */
    public int getMonth(long timestamp)
    {
        String dateString = null;
        String month = null;
        int i = 0;

        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateString = dateFormat.format(date).trim();

        if (!TextUtils.isEmpty(dateString))
        {
            month = (dateString.substring(4, 6));
        }

        try {
            i = Integer.parseInt(month);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return i;
    }



    /**
     * 获取使用的流量
     * @return size 单位KB
     */
    private int getUsedFlowMethod()
    {
        long l = TrafficStats.getMobileTxBytes();
        long n = TrafficStats.getMobileRxBytes();
        int size = (int) ((l + n)/1000);
        return size;
    }

    /**
     *
     */
    public void checkFlow()
    {
        //executor.scheduleAtFixedRate(new FlowTask(), 10, 10, TimeUnit.SECONDS);
        new Thread(flowTask).start();
    }


    private class FlowTask implements Runnable
    {
        private boolean isStop = false;

        @Override
        public void run()
        {
            while (!isStop){
                int currentMonth = getMonth(System.currentTimeMillis());
                int saveMonth = BestSpConfig.getInstance().getSaveMonth();
                if (currentMonth == saveMonth){
                    //Log.e(TAG, "run: currentMonth == saveMonth");
                    long last = getUsedFlowMethod();
                    try {
                        Thread.sleep(10*1000);
                        long now = getUsedFlowMethod();
                        long save = BestSpConfig.getInstance().getUsedDataFlow();
                        BestSpConfig.getInstance().setUsedDataFlow(now - last + save);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //Log.e(TAG, "run: currentMonth != saveMonth");
                    BestSpConfig.getInstance().setUsedDataFlow(0);
                    BestSpConfig.getInstance().setSaveMonth(currentMonth);
                    long last = getUsedFlowMethod();
                    try {
                        Thread.sleep(10*1000);
                        long now = getUsedFlowMethod();
                        long save = BestSpConfig.getInstance().getUsedDataFlow();
                        BestSpConfig.getInstance().setUsedDataFlow(now - last + save);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void setFlowLimit(long flowLimit){
        if (flowLimit == 0){
            BestSpConfig.getInstance().setDataFlowLimit((long)(150 * 1024));
        }else {
            BestSpConfig.getInstance().setDataFlowLimit(flowLimit);
        }
    }

    public long getFlowLimit(){
        return BestSpConfig.getInstance().getDataFlowLimit();
    }

    public void setFlowThreshold(long flowThreshold){
        if (flowThreshold == 0){
            LogX.d(TAG, "handle flowThreshold: is 0.00------>20.00mb");
            BestSpConfig.getInstance().setSizeThreshold((long)(20 * 1024));
        }else {
            BestSpConfig.getInstance().setSizeThreshold(flowThreshold);
        }
    }

    public long getFlowThreshold(){
        return BestSpConfig.getInstance().getSizeThreshold();
    }

    public long getFlowUsed(){
        return BestSpConfig.getInstance().getUsedDataFlow();
    }


}
