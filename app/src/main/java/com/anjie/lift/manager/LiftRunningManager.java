package com.anjie.lift.manager;


import android.os.Handler;

public class LiftRunningManager  {

    private static LiftRunningManager instance;
    private int state = 0;
    private boolean sendPause = false;
    private Handler mHandler;
    private int liftState = 0;


    private LiftRunningManager() {
//        stateThread = new StateThread();
//        stateThread.start();
    }

    public static synchronized LiftRunningManager getInstance()
    {
        if (instance == null)
        {
            instance = new LiftRunningManager();
        }
        return instance;
    }


    public void setHandler(Handler handler)
    {
        this.mHandler = handler;
    }


//    public void setState(int liftState)
//    {
//        this.liftState = liftState;
//        if (state == 0 && liftState != 0)
//        {
//            mHandler.sendEmptyMessageDelayed(UICode.PAUSE_PLAYER, 1000 *180);
//            MPlayerManager.getInstance().restartMedia();
//        }
//        else if (state !=0 && liftState != 0)
//        {
//            MPlayerManager.getInstance().isPause = true;
//            MPlayerManager.getInstance().isPlaying = true;
//            mHandler.sendEmptyMessage(UICode.RESTART_PLAYER);
//        }
//        else if (state != 0 && liftState == 0)
//        {
////            MPlayerManager.getInstance().isPause = false;
//            mHandler.sendEmptyMessageDelayed(UICode.PAUSE_PLAYER, 1000 *180);
//        }
//        else if (state == 0 && liftState == 0)
//        {
//            MPlayerManager.getInstance().isPause = false;
//            MPlayerManager.getInstance().isPlaying = false;
//        }
//        Log.e("wangxu", state + "++++++++run:====="+liftState);
//        state = liftState;
//    }


    public void setTimerTask(int liftState){
        if (state == 0 && liftState != 0)
        {
            MPlayerManager.getInstance().setPlayerRestart();
        }
        else if (state !=0 && liftState != 0)
        {
            MPlayerManager.getInstance().setTagPFalse();
        }
        else if (state != 0 && liftState == 0)
        {
            MPlayerManager.getInstance().setTagPAndSendMsg();
        }
        else if (state == 0 && liftState == 0)
        {

        }
        //Log.e("wangxu", state + "++++++++run:====="+liftState);
        state = liftState;
    }


}
