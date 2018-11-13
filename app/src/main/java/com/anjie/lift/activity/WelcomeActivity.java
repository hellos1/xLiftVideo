package com.anjie.lift.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.anjie.common.log.LogX;
import com.anjie.lift.R;
import com.anjie.lift.manager.ControlCenter;
import com.anjie.lift.manager.ViewManager;

import java.lang.ref.WeakReference;

/**
 * 欢迎页面
 */
public class WelcomeActivity extends Activity
{
    /**
     * 日志标签
     */
    private static final String TAG = "WelcomeView";

    /**
     * 初始化加载背景图
     */
    private ImageView loadingView;

    /**
     * 等待对话框
     */
    private ProgressDialog waitingDialog;

    /**
     * 加载延迟时间
     */
    private static final int TIME_DELAY = 500;

    /**
     * 初始化任务结束消息
     */
    private static final int CODE_LOADING_FINISH = 1;

    /**
     * Handler
     */
    private Handler mHandler = null;

    /**
     * 操作UI的Handler
     */
    private static class UIHandler extends Handler
    {
        /**
         * 弱应用避免内存泄漏
         */
        private WeakReference<WelcomeActivity> ref;

        public UIHandler(WelcomeActivity activity)
        {
            ref = new WeakReference<WelcomeActivity>(activity);
        }

        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            WelcomeActivity activity = ref.get();
            if (activity == null || activity.isFinishing())
            {
                return;
            }
            switch (msg.what)
            {
                case CODE_LOADING_FINISH:
                {
                    activity.closeWaitingDialog();
                    activity.goMainActivity();
                    break;
                }
                default:
                {
                    break;
                }
            }
        }

    }

    /**
     * 显示Loading对话框
     */
    private void showWaitingDialog()
    {
        if (waitingDialog == null)
        {
            waitingDialog = new ProgressDialog(this);
            waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waitingDialog.setMessage(getString(R.string.str_loading));
        }
        waitingDialog.show();
    }

    /**
     * 关闭等待对话框
     */
    private void closeWaitingDialog()
    {
        if (waitingDialog != null)
        {
            waitingDialog.dismiss();
        }
        waitingDialog = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // 调用父类
        super.onCreate(savedInstanceState);
        LogX.d(TAG, "Welcome view onCreate()");

        //获取屏幕分辨率信息,设置判断屏幕信息的ViewManager中的width;
        setScreenMsg();

        // 设置欢迎页面布局
        setContentView(R.layout.layout_welcome);
        // 构建欢迎页面图片显示控件
       // loadingView = (ImageView) findViewById(R.id.welcome_image);
        // 欢迎图片,加载企业LOGO
        //loadingView.setBackgroundResource(R.drawable.logo);
        // 初始化UIHandler
        mHandler = new UIHandler(this);
        // 显示等待对话框
        //showWaitingDialog();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                long start = System.currentTimeMillis();

                // 检查版本,检查升级要做的数据更新
                ControlCenter.getInstance().checkVersionForUpdate();

                // 所有资源转移到控制中心去初始化
                ControlCenter.getInstance().init();
                long costTime = System.currentTimeMillis() - start;
                LogX.i(TAG, "Welcome view init cost time:" + costTime + " ms");
//                if (TIME_DELAY - costTime > 0)
//                {
//                    try
//                    {
//                        Thread.sleep(TIME_DELAY - costTime);
//                    }
//                    catch (InterruptedException e)
//                    {
//                        LogX.e(TAG, "Loading meet InterruptedException", e);
//                    }
//                }
                // 初始化任务结束,由UI线程操作视图,欢迎页面持续2秒
                mHandler.sendEmptyMessage(CODE_LOADING_FINISH);
            }
        }).start();
    }

    /**
     * 设置判断屏幕的信息的width
     */
    private void setScreenMsg(){
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if (width == 800 || height == 800){
            ViewManager.getInstance().setWidth(800);
        }else {
            ViewManager.getInstance().setWidth(1024);
        }
    }

    /**
     * 跳转到主页面
     */
    private void goMainActivity()
    {
        // 构建跳转
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        // 关闭当前欢迎页面
        finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        LogX.d(TAG, "Welcome view onDestroy()");
        closeWaitingDialog();
    }
}
