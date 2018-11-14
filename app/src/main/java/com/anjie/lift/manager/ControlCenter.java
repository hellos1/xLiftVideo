package com.anjie.lift.manager;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;

import com.anjie.common.io.IOUtils;
import com.anjie.common.log.LogX;
import com.anjie.common.storage.FileCacheService;
import com.anjie.common.util.VersionInfo;
import com.anjie.lift.activity.UICode;
import com.anjie.lift.app.AppContext;
import com.anjie.lift.app.AppInfoManager;
import com.anjie.lift.app.AppPreference;
import com.anjie.lift.app.BroadcastCenter;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.config.ConfigManager;
import com.anjie.lift.service.adapter.best_screensdk.ScreenSdkConfig;
import com.anjie.lift.service.adapter.best.BestPreferences;
import com.anjie.lift.usb.USBStorageReceiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import android_serialport_api.LiftInfoListener;
import android_serialport_api.SerialPortReader;

/**
 * 电梯控制中心
 */
public class ControlCenter
{
    /**
     * 控制中心
     */
    private static final String TAG = "XControlCenter";

    /**
     * 单实例
     */
    public static ControlCenter instance = new ControlCenter();

    /**
     * 单任务线程池
     */
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    /**
     * Handler
     */
    private Handler mHandler = null;

    /**
     * USB监听器
     */
    private USBStorageReceiver usbReceiver = new USBStorageReceiver();

    /**
     * 串口数据
     */
    private SerialPortReader mSPReader = null;

    /**
     * 是否初始化
     */
    private AtomicBoolean isInit = new AtomicBoolean(false);

    /**
     * 电梯运行实时数据监听
     */
    private LiftInfoListener liftListener = new LiftInfoListener()
    {
        @Override
        public void onError()
        {
            // 电梯错误信息
        }

        @Override
        public void onDataReceive(String floorNum, int direction, byte status)
        {
            // 电梯楼层和运行方向信息
            notifyLiftInfoChange(floorNum, direction, status);
        }
    };

    /**
     * 本地广播接收器
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {

            if(intent.getAction()!=null){
                switch (intent.getAction()){
                    case BroadcastCenter.VIEW_CHANGE_ACTION:
                        // 重新加载视图
                        handleViewChange();
                        break;
                    case BroadcastCenter.VIEW_CHANGE_ACTION_LOCAL:
                        // 重新加载播放列表
                        handleViewChangeLocal();
                        break;
                    case BroadcastCenter.PLAYLIST_CHANGE_ACTION:
                        // 重新加载播放列表
                        handlePlayListChange(false);
                        break;
                    case BroadcastCenter.USB_SYNC_DATA_ACTION:
                        // USB同步开始,显示等待进度框
                        showProgressDialog();
                        break;
                    case BroadcastCenter.USB_SYNC_DATA_FINISH_ACTION:
                        // USB同步结束,关闭等待进度框
                        closeProgressDialog();
                        // 重新加载播放列表播放
                        handlePlayListChange(true);
                        break;
                    case BroadcastCenter.PLAYLIST_CHANGE_ACTION_CLOUD:
                        // 重新加载播放列表播放
                        //handlePlayListChange(true);
                        //2018.04.16
                        handleCloudPlayListChange(true);
                        break;
                    case BroadcastCenter.REFRESH_FULL_SCREEN_ACTION:
                        //2018.07.05
                        //云端更新横竖屏时，在全屏模式下，图片无法正确显示
                        refreshFullScreen();
                        break;
                    default:
                        break;
                }
            }

        }
    };

    /**
     * 处理视图变化
     */
    private void handleViewChange()
    {
        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                // 线程解析资源
                ViewManager.getInstance().loadViewRes();
                // 停止播放器
                MPlayerManager.getInstance().stopPlayTask(true);
                MPlayerManager.getInstance().has_set_display.set(false);//2018.06.20
                MPlayerManager.getInstance().setMediaVolume(ConfigManager.getInstance().getVolume());//2018.06.21
                notifyReloadView();
            }
        });
    }

    private void handleViewChangeLocal()
    {
        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                // 线程解析资源
                ViewManager.getInstance().loadViewRes();
                notifyReloadView();
            }
        });
    }

    /**
     * 通知页面重新加载布局视图
     */
    private void notifyReloadView()
    {
        if (mHandler != null)
        {
            mHandler.sendEmptyMessage(UICode.RELOADING_VIEW);
        }
    }

    /**
     * 显示同步数据等待进度框
     */
    private void showProgressDialog()
    {
        if (mHandler != null)
        {
            mHandler.sendEmptyMessage(UICode.SHOW_SYNC_DIALOG);
        }
    }

    /**
     * 关闭同步数据等待进度框
     */
    private void closeProgressDialog()
    {
        if (mHandler != null)
        {
            mHandler.sendEmptyMessage(UICode.CLOSE_SYNC_DIALOG);
        }
    }

    /**
     * 刷新页面电梯信息
     * 
     * @param floorNum
     *            楼层显示
     * @param direction
     *            运行方向
     */
    private void notifyLiftInfoChange(String floorNum, int direction, byte status)
    {
        if (mHandler != null)
        {
            Message msg = mHandler.obtainMessage(UICode.LIFT_INFO_CHANGE, direction, status, floorNum);
            mHandler.sendMessage(msg);
        }
    }

    /**
     * 重新加载播放列表
     */
    private void handlePlayListChange(final boolean is_del)
    {
        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                boolean flag = is_del;
                LogX.d(TAG, "Receive broadcast to handle play list change ");
                // 线程解析播放列表
                MPlayerManager.getInstance().loadPlayList(flag);
                //MPlayerManager.getInstance().SetInit();
               // MPlayerManager.getInstance().startPlayTask();
            }
        });
    }


    /**
     * 云端重新加载播放列表
     * 2018.04.16
     */
    //2018.04.16
    private void handleCloudPlayListChange(final boolean is_del)
    {
        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                boolean flag = is_del;
                LogX.d(TAG, "Receive broadcast to handle play list change ");
                // 线程解析播放列表
                MPlayerManager.getInstance().stopPlayTask(false);
                MPlayerManager.getInstance().has_set_display.set(false);
                MPlayerManager.getInstance().setMediaVolume(ConfigManager.getInstance().getVolume());//2018.6.21
                MPlayerManager.getInstance().loadPlayList(flag);
                MPlayerManager.getInstance().SetInit();
                MPlayerManager.getInstance().startPlayTask();
            }
        });
    }


    /**
     * refresh full screen in order to show the picture correctly
     */
    //2018.07.05
    private void refreshFullScreen()
    {
        //ViewManager.getInstance().loadViewRes();
        if (mHandler != null)
        {
            mHandler.sendEmptyMessage(UICode.REFRESH_FULL_SCREEN);
        }
    }


    /**
     * 私有构造
     */
    private ControlCenter()
    {
        mSPReader = new SerialPortReader(liftListener);
    }

    /**
     * 注册监听
     * 
     * @param handler 消息处理
     */
    public void setHandle(Handler handler)
    {
        this.mHandler = handler;
    }

    /**
     * 获取单实例
     * 
     * @return 单实例
     */
    public static ControlCenter getInstance()
    {
        return instance;
    }

    /**
     * 初始化资源
     */
    public void init()
    {
        if (isInit.getAndSet(true))
        {
            // 已经初始化了返回
            return;
        }
        // 检查本地APP的工作的文件目录
        FileManager.getInstance().initDir();
        AppInfoManager.getInstance().init();
        // 加载视图资源
        ViewManager.getInstance().loadViewRes();
        // 加载播放列表
        MPlayerManager.getInstance().loadPlayList(false);
        // 注册本地广播
        registerLocalBroadcast();
        // 注册USB监听
        registerUSBStorageReceiver();
        // 开始检测电梯数据
        mSPReader.startMonitor();

        //2018.05.18 心跳包
        mSPReader.startHeartBeats();
        //2018.03.06设置系统保存的亮度值
        setBrightness(ConfigManager.getInstance().getBrightness());
        setScreenSizeMsg();
    }


    /**
     * 根据app目录下的screen.txt文件
     * 获取屏幕的信息 10.4 or 15 (104 or 150)
     * 并保存在share preference中
     * 2018.07.11
     */
    //2018.07.11
    private void setScreenSizeMsg(){

        if (ConfigManager.getInstance().getScreenSizeMsg() != 0)
        {
            LogX.d(TAG, "screen size msg has set already" + ConfigManager.getInstance().getScreenSizeMsg());
            return;
        }

        File file = new File(FileManager.getInstance().getAppFileRoot() + "screen.txt");
        BufferedReader br = null;
        String s = null;

        if (!file.exists())
        {
            FileCacheService.copyFile("/system/app/screen.txt",
                    FileManager.getInstance().getAppFileRoot()+"screen.txt");
        }

        try {
            br = new BufferedReader(new FileReader(file));
            s = br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            IOUtils.close(br);
        }

        ConfigManager.getInstance().setScreenSizeMsg(Integer.valueOf(s));
        //ConfigManager.getInstance().setScreenSizeMsg(150);
        LogX.d(TAG, "set ScreenSize: "+ConfigManager.getInstance().getScreenSizeMsg());
    }


    private void setScreenSdkConfig(){
        ScreenSdkConfig sdkConfig = new ScreenSdkConfig();
    }


    /**
     * 注册USB插拔的监听
     */
    private void registerUSBStorageReceiver()
    {
        IntentFilter filter = new IntentFilter();
        // USB插入和拔出的两个系统广播
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        // 这个DataScheme一定要加,否则监听不到
        filter.addDataScheme("file");
        // 注册监听
        AppContext.getInstance().getContext().registerReceiver(usbReceiver,filter);
    }

    /**
     * 注册本地广播
     */
    private void registerLocalBroadcast()
    {
        LogX.d(TAG, "register BroadcastReceiver.");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastCenter.PLAYLIST_CHANGE_ACTION);
        filter.addAction(BroadcastCenter.VIEW_CHANGE_ACTION);
        filter.addAction(BroadcastCenter.VIEW_CHANGE_ACTION_LOCAL);
        filter.addAction(BroadcastCenter.USB_SYNC_DATA_ACTION);
        filter.addAction(BroadcastCenter.USB_SYNC_DATA_FINISH_ACTION);
        filter.addAction(BroadcastCenter.PLAYLIST_CHANGE_ACTION_CLOUD);
        filter.addAction(BroadcastCenter.REFRESH_FULL_SCREEN_ACTION);
        Context context = AppContext.getInstance().getContext();
        // 注册进程内部广播
        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, filter);
    }

    /**
     * APP恢复出厂设置
     */
    public void resetFactoryMode()
    {
        // 1. 停止播放器
        MPlayerManager.getInstance().stopPlayTask(true);
        MPlayerManager.getInstance().has_set_display.set(false);
        //设置休眠模式
        MPlayerManager.getInstance().bootPlay.getAndSet(true);

        // 2. 删除elevator 下所有文件
        String rootDir = FileManager.getInstance().getAppFileRoot();
        FileCacheService.deleteFileDir(rootDir);

        // 3. 重新初始化文件目录结构
        FileManager.getInstance().initDir();

        // 4. 重新初始化配置信息
        AppInfoManager.getInstance().init();

        // 重置播放列表
        MPlayerManager.getInstance().resetPlayList();
        // 清除布局数据
        ViewManager.getInstance().resetAllLayout();
        // 清除配置数据
        ConfigManager.getInstance().resetDefault();
        // 清空Best数据
        new BestPreferences().ClearCache();
        notifyReloadView();
    }

    /**
     * 更新标题内容
     * 
     * @param title 标题
     */
    public void notifyTitleChange(String title)
    {
        mHandler.sendMessage(mHandler.obtainMessage(UICode.UPDATE_TITLE, title));
    }

    /**
     * 更新滚动文字内容
     * @param context 滚动文字
     */
    public void notifyScrollTextChange(String context)
    {
        mHandler.sendMessage(mHandler.obtainMessage(UICode.AD_CONTENT_CHANGE, context));
    }

    private int brightSetting = 50;

    /**
     * 设置系统亮度
     * 
     * @param brightness 亮度值
     */
    public void setBrightness(int brightness)
    {
        //2018.03.01增加362行 KONE设置范围是0到100,安卓系统可调范围是0到255
        brightSetting = (int) (brightness * 2.55);
        Context context = AppContext.getInstance().getContext();
        Uri uri = Settings.System.getUriFor("color_brightness");
        Settings.System.putInt(context.getContentResolver(), "color_brightness", brightSetting);
        context.getContentResolver().notifyChange(uri, null);

//        Uri insertUri = Uri.parse("content://com.android.settings.anjie/brightness");
//        // 使用ContentValues传递一些数据
//        ContentValues values = new ContentValues();
//        values.put("brightness", brightness);
//        ContentResolver contentResolver = context.getContentResolver();
//        if (contentResolver != null)
//        {
//            contentResolver.update(uri, values, null, null);
//        }

        Uri insertUri = Uri.parse("content://com.android.settings.anjie");
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver != null)
        {
            Bundle extras = new Bundle();
            extras.putInt("brightnessValue", brightness);
            contentResolver.call(insertUri, "brightness", "request", extras);
            //这个地方我们用call的方法调用,可以使用Bundle传递一些数据
        }
    }

    /**
     * 检查升级是否要更新数据
     */
    public void checkVersionForUpdate()
    {
        // Android Context
        Context context = AppContext.getInstance().getContext();
        AppPreference appPreference = new AppPreference(context);
        // 缓存的APK版本号
        int versionCode = appPreference.getVersionCode();
        int currentVersionCode = new VersionInfo().getVersionCode();
        LogX.d(TAG, "apkVersionCode:" + currentVersionCode + ",cacheVersionCode:" + versionCode);
        if (versionCode == 0)
        {
            // 如果缓存的APK版本号是0，说明软件第一次运行或者APP应用被->设置->软件管理，清除过数据
            ViewManager.getInstance().clearAllLayout();
            // 缓存版本号
            appPreference.cacheVersionCode(currentVersionCode);
        }
        else if (currentVersionCode > versionCode)
        {
            // 版本升级 清除所有布局文件和布局目录,并且更新缓存版本号
            ViewManager.getInstance().clearAllLayout();
            // 缓存版本号
            appPreference.cacheVersionCode(currentVersionCode);
        }
    }
}
