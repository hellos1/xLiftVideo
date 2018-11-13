package com.anjie.lift.app;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;

import com.anjie.common.log.LogX;
import com.anjie.common.storage.FileCacheService;
import com.anjie.lift.R;
import com.anjie.lift.config.ElevatorInfo;
import com.anjie.lift.parse.ConfigParser;
import com.anjie.lift.utils.AppUtils;

import java.io.File;

import android_serialport_api.LiftInfo;

/**
 * APK的信息
 */
public final class AppInfoManager
{
    /**
     * 日志标签
     */
    private static final String TAG = "AppInfo";

    /**
     * 电梯配置信息
     */
    private ElevatorInfo elevatorInfo;

    /**
     * 单实例
     */
    private static AppInfoManager instance = new AppInfoManager();

    /**
     * 字库字体
     */
    private Typeface typeFace;

    /**
     * 电梯上行图标
     */
    private Drawable directionUp;

    /**
     * 电梯下行图标
     */
    private Drawable directionDown;

    /**
     * 电梯抵达图标
     */
    private Drawable directionArrive;

    /**
     * 司机服务
     */
    private Drawable statusATS;

    /**
     * 消防运行
     */
    private Drawable statusFRS;

    /**
     * 退出服务开关
     */
    private Drawable statusOSS;

    /**
     * 超载
     */
    private Drawable statusOLS;

    /**
     * 内呼优先服务
     */
    private Drawable statusPRS;

    /**
     * 空
     */
    private Drawable statusNMS;

    /**
     * 离线Offline
     */
    private Drawable statusOFL;

    /**
     * 私有构造
     */
    private AppInfoManager()
    {

    }

    /**
     * 获取项目的PID信息
     * 
     * @return
     */
    public String getPid()
    {
        if (elevatorInfo != null)
        {
            return elevatorInfo.getPid();
        }
        return null;
    }

    /**
     * 获取项目的DID信息
     * 
     * @return
     */
    public String getDid()
    {
        if (elevatorInfo != null)
        {
            return elevatorInfo.getDid();
        }
        return null;
    }

    /**
     * 单实例
     * 
     * @return
     */
    public static AppInfoManager getInstance()
    {
        return instance;
    }

    /**
     * 获取资源云服务器地址
     * 
     * @return
     */
    public String getServerHost()
    {
        String serverHost = null;
        if (elevatorInfo != null)
        {
            serverHost = elevatorInfo.getServer();
        }
        if (serverHost == null)
        {
            return null;
        }
        // 兼容处理服务器地址带了/(http://139.196.15.189/)
        if (serverHost.endsWith("/"))
        {
            serverHost = serverHost.substring(0, serverHost.length() - 1);
        }
        return serverHost;
    }

    /**
     * 获取下载服务器地址
     * 
     * @return
     */
    public String getDownloadHost()
    {
        // 目前使用同一个服务器地址
        return getServerHost();
    }

    /**
     * 获取字体格式
     * 
     * @return
     */
    public Typeface getTypeFace()
    {
        if (typeFace == null)
        {
            // 使用默认的
            typeFace = Typeface.createFromAsset(AppContext.getInstance().getContext().getAssets(), "myfont.otf");
        }
        return typeFace;
    }

    /**
     * 初始化程序的工作
     */
    public void init()
    {
        initElevatorInfo();
        initTypeFace();
        initDirectionDrawable();
    }

    /**
     * 解析elevator.xml信息
     */
    private void initElevatorInfo()
    {
        String configFile = FileManager.getInstance().getElevatorConfigFile();
        File f = new File(configFile);
        if (!f.exists())
        {
            Context context = AppContext.getInstance().getContext();
            // 配置文件不存在,从程序assert目录写入一份默认的，到工作目录
            try
            {
                FileCacheService.writeFile(context.getAssets().open("elevator.xml"), configFile);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to copy elevator.xml to app folder.");
            }
        }
        elevatorInfo = ConfigParser.parseElevatorInfo(configFile);
        LogX.d(TAG, "ElevatorInfo:" + elevatorInfo);
        if (elevatorInfo != null)
        {
            int rot = elevatorInfo.getRot();
            // android.os.SystemProperties.set("persist.sys.hwrotation",
            // String.valueOf(rot));

            // 反射调用系统接口
            // Context context = AppContext.getInstance().getContext();
            // SystemPropertiesProxy.set(context, "persist.sys.hwrotation",
            // String.valueOf(rot));
            LogX.d(TAG, "Set rot:" + String.valueOf(rot));
        }
    }

    /**
     * 是否是全屏模式
     * 
     * @return
     */
    public boolean isFullScreenMode()
    {
        if (elevatorInfo != null)
        {
            // TODO 全屏模式，安杰自己的和KONE的模式
        }
        return false;
    }

    /**
     * 初始化字体库
     */
    public boolean initTypeFace()
    {
        // 字体配置文件是否存在
        String fontConfigFile = FileManager.getInstance().getFontConfigFile();
        String fontName = ConfigParser.parseFontFileName(fontConfigFile);
        if (TextUtils.isEmpty(fontName))
        {
            return false;
        }
        String fontFileFullPath = FileManager.getInstance().getCusFontDir() + fontName;
        File file = new File(fontFileFullPath);
        if (!file.exists())
        {
            return false;
        }
        try
        {
            typeFace = Typeface.createFromFile(fontFileFullPath);
        }
        catch (Exception ex)
        {
            // 加载失败会抛出RuntimeException异常
            LogX.d(TAG, "load custom font file meet exception.");
            typeFace = null;
            return false;
        }
        return true;
    }

    /**
     * 加载默认的电梯图标图标
     */
    private void loadDefaultIcon()
    {
        Context context = AppContext.getInstance().getContext();
        directionUp = context.getResources().getDrawable(R.drawable.up);
        directionDown = context.getResources().getDrawable(R.drawable.down);
        directionArrive = context.getResources().getDrawable(R.drawable.none);
    }

    /**
     * 加载电梯默认的运行方向资源
     */
    public void initDirectionDrawable()
    {
        String liftIconConfigFile = FileManager.getInstance().getLiftIconConfigFile();
        Bundle bundle = ConfigParser.parseLiftIconConfigFile(liftIconConfigFile);
        String upFileName = bundle.getString("UpFileName", null);
        String downFileName = bundle.getString("DownFileName", null);
        String arriveFileName = bundle.getString("ArriveFileName", null);
        if (TextUtils.isEmpty(upFileName) || TextUtils.isEmpty(downFileName) || TextUtils.isEmpty(arriveFileName))
        {
            // 自定义的自要有一个为空，就使用默认的
            loadDefaultIcon();
            LogX.d(TAG, "init lift direction icon,custom null,use default.");
            return;
        }
        String iconDir = FileManager.getInstance().getLiftIconDir();
        File upFile = new File(iconDir, upFileName);
        File downFile = new File(iconDir, downFileName);
        File arriveFile = new File(iconDir, arriveFileName);

        if (upFile.exists() && downFile.exists() && arriveFile.exists())
        {
            Context context = AppContext.getInstance().getContext();
            directionUp = new BitmapDrawable(context.getResources(), upFile.getPath());
            directionDown = new BitmapDrawable(context.getResources(), downFile.getPath());
            directionArrive = new BitmapDrawable(context.getResources(), arriveFile.getPath());
        }
        else
        {
            loadDefaultIcon();
            LogX.w(TAG, "init lift direction icon fileName do not exist.");
        }
    }

    /**
     * 获取电梯运行状态图片
     * 
     * @param status
     * @return
     */
    public Drawable getStatusDrawable(byte status)
    {
        // none
        if ((status & 0xff) == 0xff)
        {
            if (statusNMS == null)
            {
                statusNMS = AppContext.getInstance().getContext().getResources().getDrawable(R.drawable.none);
            }
            if (statusOFL == null)
            {
                statusOFL = AppContext.getInstance().getContext().getResources().getDrawable(R.drawable.none);
            }

            // 单机版本或者网络未连接,需要显示离线
            if (getNetType() == 2 || !AppUtils.isNetworkConnected(AppContext.getInstance().getContext()))
            {
                return statusOFL;
            }
            return statusNMS;
        }

        // 内呼优先服务
        if ((status & 0xff) == 0x86)
        {
            if (statusPRS == null)
            {
                statusPRS = AppContext.getInstance().getContext().getResources().getDrawable(R.drawable.prcc);
            }
            return statusPRS;
        }

        // 退出服务开关
        if ((status & 0xff) == 0x87 || (status & 0xff) == 0x84 || (status & 0xff) == 0x88)
        {
            if (statusOSS == null)
            {
                statusOSS = AppContext.getInstance().getContext().getResources().getDrawable(R.drawable.ossc);
            }
            return statusOSS;
        }

        // 司机服务
        if ((status & 0xff) == 0x89)
        {
            if (statusATS == null)
            {
                statusATS = AppContext.getInstance().getContext().getResources().getDrawable(R.drawable.atsc);
            }
            return statusATS;
        }

        // 消防运行
        if ((status & 0xff) == 0x81)
        {
            if (statusFRS == null)
            {
                statusFRS = AppContext.getInstance().getContext().getResources().getDrawable(R.drawable.frsc);
            }
            return statusFRS;
        }

        // 超载
        if ((status & 0xff) == 0x82)
        {
            if (statusOLS == null)
            {
                statusOLS = AppContext.getInstance().getContext().getResources().getDrawable(R.drawable.overloadc);
            }
            return statusOLS;
        }
        return null;
    }

    /**
     * 获取电梯运行方法图标
     * 
     * @param direction
     * @return
     */
    public Drawable getDirectionDrawable(int direction)
    {
        if (direction == LiftInfo.DIRECTION_UP)
        {
            if (directionUp == null)
            {
                directionUp = AppContext.getInstance().getContext().getResources().getDrawable(R.drawable.up);
            }
            return directionUp;
        }
        else if (direction == LiftInfo.DIRECTION_DOWN)
        {
            if (directionDown == null)
            {
                directionDown = AppContext.getInstance().getContext().getResources().getDrawable(R.drawable.down);
            }
            return directionDown;
        }
        else if (direction == LiftInfo.DIRECTION_ARRIVE)
        {
            if (directionArrive == null)
            {
                directionArrive = AppContext.getInstance().getContext().getResources().getDrawable(R.drawable.none);
            }
            return directionArrive;
        }
        return null;
    }

    /**
     * 获取云的类型
     * 
     * @return
     */
    public int getCloudType()
    {
        if (elevatorInfo != null)
        {
            return elevatorInfo.getCloudType();
        }
        return 0;
    }

    /**
     * 获取网络类型(-1:未知，0-4G;1-以太网,2-单机)
     *
     * @return
     */
    public int getNetType()
    {
        if (elevatorInfo != null)
        {
            return elevatorInfo.getNet();
        }
        return 2;
    }
}
