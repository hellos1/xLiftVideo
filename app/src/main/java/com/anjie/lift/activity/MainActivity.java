package com.anjie.lift.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.anjie.common.log.LogX;
import com.anjie.common.system.SystemPropertiesProxy;
import com.anjie.lift.R;
import com.anjie.lift.app.AppContext;
import com.anjie.lift.app.AppInfoManager;
import com.anjie.lift.config.ConfigManager;
import com.anjie.lift.manager.ControlCenter;
import com.anjie.lift.manager.LiftRunningManager;
import com.anjie.lift.manager.MPlayerManager;
import com.anjie.lift.manager.ScreenAdapter;
import com.anjie.lift.manager.ViewManager;
import com.anjie.lift.player.PlayerElement;
import com.anjie.lift.service.ResourceService;
import com.anjie.lift.view.BaseView;
import com.anjie.lift.view.BaseView.ViewType;
import com.anjie.lift.view.MarqueeTextView;
import com.anjie.lift.view.PlayerView;
import com.anjie.lift.view.Position;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 播放主界面
 */
public class MainActivity extends Activity
{
    /**
     * 日志标签
     */
    private static final String TAG = "MainView";

    /**
     * Root相对布局
     */
    private AbsoluteLayout rootLayout = null;

    /**
     * 标题区域
     */
    private TextView titleRegion;

    /**
     * 电梯运行方向
     */
    private ImageView directionRegion;

    /**
     * 电梯运行状态
     */
    private ImageView statusRegion;

    /**
     * 电梯显示楼层
     */
    private TextView floorNumRegion;

    /**
     * 播放区域(视频，图片和WebView)
     */
    private PlayerRegion mPlayerRegion;

    /**
     * 是否正在加载视图
     */
    private AtomicBoolean isLoadingView = new AtomicBoolean(false);

    /**
     * 等待进度框
     */
    private ProgressDialog waitingDialog;

    /**
     * 移动广告文字
     */
    private MarqueeTextView adTextView;

    /**
     * 时间控件
     */
    private TextClock timeView = null;

    /**
     * 日期控件
     */
    private TextClock dateView = null;

    Handler mHandler = new UIHandler(this);

    /**
     * 二维码信息视图
     */
    private QRCodeInfoView qrCodeInfoView = new QRCodeInfoView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogX.i(TAG, "onCreate()");
        // 设置布局
        setContentView(R.layout.layout_main);
        // 根布局
        rootLayout = (AbsoluteLayout) findViewById(R.id.root_layout);
        // 调用页面初始化
        initViewMode(true);

        // 启动后台Service
        Intent startIntent = new Intent(this, ResourceService.class);
        startService(startIntent);
        // UI刷新Handler
        //Handler mHandler = new UIHandler(this);
        MPlayerManager.getInstance().setMediaPlayer(mHandler);
        ControlCenter.getInstance().setHandle(mHandler);
        LiftRunningManager.getInstance().setHandler(mHandler);
        LogX.w(TAG, "MainActivity has onCreated");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LogX.d(TAG, "onResume()");
        if (mPlayerRegion != null)
        {
            mPlayerRegion.onResume();
        }
        // 页面恢复,开始播放任务
        MPlayerManager.getInstance().SetInit();//2018.05.11
        MPlayerManager.getInstance().startPlayTask();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        LogX.d(TAG, "onPause()");
        if (mPlayerRegion != null)
        {
            mPlayerRegion.onPause();
        }

        MPlayerManager.getInstance().stopPlayTask(false);
    }

    /**
     * 初始化播放区域
     *
     * @param isHiddenTop
     *            是否隐藏顶部
     * @param isHiddenBottom
     *            是否隐藏底部
     */
    private void initPlayView(boolean isHiddenTop, boolean isHiddenBottom)
    {
        if (!isHiddenTop && !isHiddenBottom)
        {
            // 正常模式
            BaseView baseView = ViewManager.getInstance().getView(ViewType.Player);
            if (baseView != null)
            {
                initPlayView((PlayerView) baseView);
            }
            return;
        }
        Context context = AppContext.getInstance().getContext();
        String rot =  SystemPropertiesProxy.get(context,"persist.sys.hwrotation");
        boolean is_90=false;
        if(rot.equals("0") || rot.equals("180")) {
            // 只拉升上面
            is_90=false;
            //2018.07.11

        }else if(rot.equals("270") || rot.equals("90")) {
            is_90=true;
            // 只拉升上面 2018.05.23   90-->270

        }else {
            // 只拉升上面
            is_90=false;

        }
        PlayerView extPlayView;
        if (isHiddenTop && isHiddenBottom)
        {
            // 上下拉升
            extPlayView = ViewHelper.getExtTopBottomPlayView(is_90);
            initPlayView(extPlayView);
            return;
        }

        if (isHiddenTop) {

            extPlayView = ViewHelper.getExtTopPlayView(is_90);

            initPlayView(extPlayView);
            return;
        }

        if (isHiddenBottom)
        {
            // 只拉升底部
            extPlayView = ViewHelper.getExtBottomPlayView();
            initPlayView(extPlayView);
        }
    }

    /**
     * 初始化播放区域
     */
    private void initPlayView(PlayerView playerView)
    {
        if (playerView == null)
        {
            LogX.w(TAG, "init play view is null.");
            return;
        }
        // 播放区域坐标区
        Position playPosition = playerView.getPosition();
        if (playPosition == null)
        {
            LogX.w(TAG, "init play view position is null.");
            return;
        }

        LogX.d(TAG, "init play view Position:" + playPosition);
        mPlayerRegion = new PlayerRegion(this, rootLayout, playPosition);
    }

    /**
     * 初始化整个布局视图模式
     */
    private void initViewMode(boolean is_first)
    {
        LogX.d(TAG, "initViewMode()");

        LogX.d("DisplayType", "This is init view mode and get the hwrotation"
                + SystemPropertiesProxy.get(this,"persist.sys.hwrotation"));

        // 释放WebView,以免内存泄漏
        if (mPlayerRegion != null)
        {
            mPlayerRegion.onFreeWebView();
        }

        // 首先清除所有布局
        rootLayout.removeAllViews();

        if (ConfigManager.getInstance().isFullScreenMode())
        {
            // 全屏模式
            initFullScreenPlay();
        }
        else
        {
            // 非全屏模式
            initNormalViewMode();
        }
        if(is_first==true) {
            // 开机或者重新加载布局的时候显示MAC地址的二维码功能
            qrCodeInfoView.showMacQRCodeView(this, rootLayout);
        }
    }

    /**
     * 初始化全屏
     */
    private void initFullScreenPlay()
    {
        // 1.加载整个屏幕背景资源:1优先图片,2其次RGB颜色
        ViewHelper.initBackground(rootLayout);
        // 2.加载播放区域
        PlayerView playerView = new PlayerView();
        WindowManager wm = this.getWindowManager();
        // 获取屏幕的宽度和高度
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        LogX.w(TAG,"LIUCHUN"+"FUL");
        playerView.setPosition(new Position(0, 0, dm.widthPixels, dm.heightPixels));
        LogX.d(TAG, "Screen:" + dm.widthPixels + "*" + dm.heightPixels);
        initPlayView(playerView);
    }

    /**
     * 是否隐藏时间
     *
     * @param isHidden
     *            是否隐藏时间
     */
    private void setHiddenTime(boolean isHidden)
    {
        int value = isHidden ? View.GONE : View.VISIBLE;
        if (timeView != null)
        {
            timeView.setVisibility(value);
            timeView.invalidate();
        }

        if (dateView != null)
        {
            dateView.setVisibility(value);
            dateView.invalidate();
        }
    }

    /**
     * 是否隐藏标题
     *
     * @param isHidden
     *            是否隐藏标题
     */
    private void setHiddenTitle(boolean isHidden)
    {
        if (titleRegion != null)
        {
            int visible = isHidden ? View.GONE : View.VISIBLE;
            titleRegion.setVisibility(visible);
            titleRegion.invalidate();
            LogX.d(TAG, "setHiddenTitle:" + isHidden);
        }
    }

    /**
     * 是否隐藏滚动文字
     *
     * @param isHidden
     *            是否隐藏滚动文字
     */
    private void setHiddenADText(boolean isHidden)
    {
        if (adTextView != null)
        {
            int visible = isHidden ? View.GONE : View.VISIBLE;
            adTextView.setVisibility(visible);
            adTextView.invalidate();
            LogX.d(TAG, "setHiddenADText:" + isHidden);
        }
    }

    /**
     * 初始化普通视图布局
     */
    private void initNormalViewMode()
    {
        LogX.i(TAG, "initNormalViewMode");
        // 1.加载整个屏幕背景资源:1优先图片,2其次RGB颜色
        ViewHelper.initBackground(rootLayout);

        boolean isHiddenTimer = ConfigManager.getInstance().isHiddenTimer();
        boolean isHiddenTitle = ConfigManager.getInstance().isHiddenTitle();
        boolean isHiddenADText = ConfigManager.getInstance().isHiddenScrollText();
        boolean isHiddenTop = isHiddenTimer && isHiddenTitle;

        // 2.初始化时间日期等格式布局,支持多标签,返回控件的ID和时间日期格式
        timeView = ViewHelper.initTimeView(rootLayout, this);
        dateView = ViewHelper.initDateView(rootLayout, this);

        setHiddenTime(isHiddenTimer);

        // 3.初始化标题的视图
        initTitleView();
        setHiddenTitle(isHiddenTitle);

        // 4.移动广告文字
        initADTextView();
        setHiddenADText(isHiddenADText);

        // 5. 电梯运行信息
        initLiftView(isHiddenTop);

        // 6.播放视图区域
        initPlayView(isHiddenTop, isHiddenADText);

        // 7.其他自定义文本区域
        ViewHelper.initCustomTextView(rootLayout, this);
        ViewHelper.initCustomImageView(rootLayout, this);
    }

    /**
     * 初始化标题内容
     */
    private void initTitleView()
    {
        titleRegion = ViewHelper.initTitleView(rootLayout, this);
        LogX.d(TAG, "initTitleView:" + titleRegion);
    }

    /**
     * 初始化广告文字控件
     */
    private void initADTextView()
    {
        adTextView = ViewHelper.initADTextView(rootLayout, this);
        LogX.d(TAG, "initADTextView:" + adTextView);
    }

    /**
     * 初始化电梯运行显示数据
     */
    private void initLiftView(boolean isHiddenTop)
    {
        Context context = AppContext.getInstance().getContext();
        String rot =  SystemPropertiesProxy.get(context,"persist.sys.hwrotation");
        boolean is_90=false;
        if(rot.equals("0") || rot.equals("180")) {
            // 只拉升上面
            is_90=false;

        }else if(rot.equals("270") || rot.equals("90")) {
            is_90=true;
            // 只拉升上面  2018.05.23  90----->270

        }else {
            // 只拉升上面
            is_90=false;

        }
        float diff = 0;
        if(is_90==true)
        {
            diff=78;
        }

        // 初始化电梯运行方法箭头
        directionRegion = ViewHelper.initDirectionView(this);
        if (directionRegion != null)
        {
            if(isHiddenTop==true) {
                float y=directionRegion.getY();
                y-=diff;
                directionRegion.setY(y);
            }
            //directionRegion.setBackgroundResource(R.drawable.down);
            rootLayout.addView(directionRegion);
        }

        // 初始化电梯状态图标
        statusRegion = ViewHelper.initStatusView(this);
        if (statusRegion != null)
        {
            if(isHiddenTop==true) {
                float y=statusRegion.getY();
                y-=diff;
                statusRegion.setY(y);
            }
            //statusRegion.setBackgroundResource(R.drawable.atsc);
            //test change for svn
            rootLayout.addView(statusRegion);
        }

        // 初始化电梯显示楼层数据视图
        floorNumRegion = ViewHelper.initFloorNumView(this);
        if (floorNumRegion != null)
        {
            if(isHiddenTop==true) {
                float y=floorNumRegion.getY();
                y-=diff;
                floorNumRegion.setY(y);
            }
//            floorNumRegion.setTextSize(ScreenAdapter.getInstance().getFloorTextSize(2));
//            floorNumRegion.setText("268");
            rootLayout.addView(floorNumRegion);
        }
    }

    /**
     * Handler刷新UI数据
     */
    private static class UIHandler extends Handler
    {
        private WeakReference<MainActivity> weakRef = null;

        UIHandler(MainActivity activity)
        {
            weakRef = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            MainActivity activity = weakRef.get();
            if (activity == null || activity.isFinishing())
            {
                return;
            }
            switch (msg.what)
            {
                case UICode.PLAY_ELEMENT:
                    activity.playElement((PlayerElement) msg.obj);
                    break;
                case UICode.RELOADING_VIEW:
                    activity.refreshNewView();
                    break;
                case UICode.LIFT_INFO_CHANGE:
                    activity.refreshLiftInfo(msg);
//                    LiftRunningManager.getInstance().setState(msg.arg1);
                    LiftRunningManager.getInstance().setTimerTask(msg.arg1);

                    break;
                case UICode.UPDATE_TITLE:
                    activity.updateTitle(msg.obj);
                    break;
                case UICode.AD_CONTENT_CHANGE:
                    activity.updateADTextContent(msg.obj);
                    break;
                case UICode.SHOW_SYNC_DIALOG:
                    activity.showProgressDialog();
                    break;
                case UICode.CLOSE_SYNC_DIALOG:
                    activity.closeProgressDialog();
                    break;
                case UICode.REFRESH_FULL_SCREEN:
                    activity.refreshFullScreen();
                    break;
                case UICode.PAUSE_PLAYER:
                    //MPlayerManager.getInstance().pauseMedia();
                    MPlayerManager.getInstance().setPlayerPause();
                    break;
                case UICode.RESTART_PLAYER:

                default:
                    break;
            }
        }
    }



    /**
     * 播放元素
     *
     * @param element
     *            播放元素
     */
    private void playElement(PlayerElement element)
    {
        if (isLoadingView.get())
        {
            return;
        }
        // 播放区域处理播放元素
        if (mPlayerRegion != null)
        {
            mPlayerRegion.playElement(element);
        }
    }

    /**
     * 刷新新的UI
     */
    private void refreshNewView()
    {
        // 忽略其他任务刷新界面
        isLoadingView.set(true);
        // 重新加载视图并刷新
        initViewMode(false);
        // 视图加载完毕
        isLoadingView.set(false);

        // 视图加载完毕之后,要继续播放任务
        MPlayerManager.getInstance().SetInit();//2018.05.10
        MPlayerManager.getInstance().startPlayTask();
    }

    //2018.07.05解决图片在全屏模式下旋转时  图片不能正常显示的问题；
    private void refreshFullScreen()
    {
        isLoadingView.set(true);
        initViewMode(false);
        isLoadingView.set(false);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        MPlayerManager.getInstance().stopPlayTask(true);
        LogX.d(TAG, "onDestroy()");
        if (mPlayerRegion != null)
        {
            mPlayerRegion.onDestroy();
        }

        // 是否二维码资源
        qrCodeInfoView.onDestroy();
    }

    /**
     * 刷新电梯运行数据显示
     *
     * @param msg
     *            电梯运行数据
     */
    private void refreshLiftInfo(Message msg)
    {
        if (isLoadingView.get())
        {
            // 如果正在加载视图刷新UI
            return;
        }
        Drawable directionDrawable = AppInfoManager.getInstance().getDirectionDrawable(msg.arg1);
        // 显示电梯运行方向
        if (directionRegion != null)
        {
            directionRegion.setBackground(directionDrawable);
        }
        if (floorNumRegion != null && msg.obj instanceof String)
        {
            String floor = (String) msg.obj;
            floor=floor.trim();
           // if (floor.length() == 1)
            //{
               // Float xxx= Float.parseFloat("0.0");
              //  xxx-=28;
              //  floorNumRegion.setX(xxx);
           // }
           // else if (floor.length() == 2)
           // {
              //  Float xxx= Float.parseFloat("0.0");
              //  floorNumRegion.setX(xxx);
          //  }
            // 显示楼层数据
            //floorNumRegion.setText(floor);
            // 显示楼层数据
            floorNumRegion.setTextSize(ScreenAdapter.getInstance().getFloorTextSize(floor.length()));
            floorNumRegion.setText(floor);
        }
        byte status = (byte) (msg.arg2 & 0xff);
        // LogX.d(TAG, "Status is :" + Integer.toHexString(status));

        Drawable statusDrawable = AppInfoManager.getInstance().getStatusDrawable(status);
        if (statusRegion != null)
        {
            statusRegion.setBackground(statusDrawable);
        }
    }

    /**
     * 更新广告马灯内容
     *
     * @param obj
     *            滚动文字文本
     */
    private void updateADTextContent(Object obj)
    {
        if (adTextView != null && obj instanceof String)
        {
            adTextView.setADText((String) obj);
        }
    }

    /**
     * 更新标题内容
     *
     * @param obj
     *            标题文字文本
     */
    private void updateTitle(Object obj)
    {
        if (titleRegion != null && obj instanceof String)
        {
            titleRegion.setText((String) obj);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        // 屏蔽按键事件
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // 屏蔽按键事件
        return true;
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog()
    {
        if (waitingDialog == null)
        {
            waitingDialog = new ProgressDialog(this);
            waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waitingDialog.setMessage(getString(R.string.str_loading_data));
        }
        waitingDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog()
    {
        if (waitingDialog != null)
        {
            waitingDialog.dismiss();
            waitingDialog = null;
        }
    }
}
