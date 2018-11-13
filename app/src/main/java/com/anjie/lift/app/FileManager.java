package com.anjie.lift.app;

import com.anjie.common.log.LogX;
import com.anjie.common.storage.SystemStorage;

import java.io.File;

/**
 * 程序文件管理
 */
public class FileManager
{
    /**
     * 日志标签
     */
    private static final String TAG = "FileManager";

    /**
     * 文件管理
     */
    private static FileManager instance = null;

    /**
     * 系统存储
     */
    private SystemStorage mStorage;

    /**
     * 本应用工作目录
     */
    private static final String WORK_DIR = "elevator";

    /**
     * 视频存放目录
     */
    public static final String VIDEO_DIR = "video";

    /**
     * 音频存放目录
     */
    public static final String AUDIO_DIR = "audio";

    /**
     * 图片存放目录
     */
    public static final String IMAGE_DIR = "image";

    /**
     * 布局视图目录
     */
    private static final String LAYOUT_DIR = "layout";

    /**
     * 布局目录下的默认布局目录名
     */
    private static final String LAYOUT_DEFAULT_DIR = "default";

    /**
     * 资源目录(存放字库和电梯图标)
     */
    private static final String RES_DIR = "res";

    /**
     * 字体目录
     */
    private static final String FONT_DIR = RES_DIR + File.separator + "font";

    /**
     * 电梯运行图标信息
     */
    private static final String LIFT_DIR = RES_DIR + File.separator + "lift";

    /**
     * 布局属性配置文件
     */
    private static final String CUSTOM_VIEW_CONFIG = "layoutConfig.xml";

    /**
     * 自定义播放列表的文件名
     */
    public static final String PLAY_LIST_NAME = "playlist.xml";

    /**
     * 电梯图标信息配置文件
     */
    private static final String LIFT_DRAWABLE_CONFIG = "liftIcon.xml";

    /**
     * 字体配置文件
     */
    private static final String CUSTOM_FONT_CONFIG = "fontConfig.xml";

    /**
     * 电梯配置文件
     */
    private static final String ELEVATOR_CONFIG = "elevator.xml";

    /**
     * APK 默认的名称
     */
    public static final String APK_NAME = "LiftVideo.apk";

    /**
     * 默认横屏布局
     */
    public static final String VIEW_FILE_NAME = "layout.xml";

    /**
     * 竖屏布局
     */
    public static final String VIEW_FILE_NAME_90 = "layout90.xml";

    /**
     * 文件管理
     */
    private FileManager()
    {
        mStorage = new SystemStorage(AppContext.getInstance().getContext());
    }

    /**
     * 获取单实例
     * 
     * @return
     */
    public synchronized static FileManager getInstance()
    {
        if (instance == null)
        {
            instance = new FileManager();
        }
        return instance;
    }

    /**
     * 获取内部存储路径
     * 
     * @return
     */
    private String getInnerStoragePath()
    {
        return mStorage.getInternelStorageDirectory();
    }

    /**
     * 初始化目录
     */
    public void initDir()
    {
        // elevator
        // -------|---image
        // -------|---video
        // -------|---layout
        // -------|---res
        // ------------|---font
        // ------------|---lift
        // 获取APP的根目录
        String appPath = getAppFileRoot();
        // 视频目录,存放播放的视频
        String videoPath = appPath + VIDEO_DIR;
        String imagePath = appPath + IMAGE_DIR;
        String layoutPath = appPath + LAYOUT_DIR;
        String resPath = appPath + RES_DIR;

        String fontPath = appPath + FONT_DIR;
        String liftPath = appPath + LIFT_DIR;
        String audioPath = appPath + AUDIO_DIR;

        makeDir(appPath);
        makeDir(videoPath);
        makeDir(imagePath);
        makeDir(layoutPath);
        makeDir(resPath);
        makeDir(fontPath);
        makeDir(liftPath);
        makeDir(audioPath);
    }

    /**
     * 创建文件夹
     * 
     * @param fileDirPath
     */
    public static boolean makeDir(String fileDirPath)
    {
        boolean isSuccess = true;
        File fileDir = new File(fileDirPath);
        try
        {
            if (fileDir.exists())
            {
                if (!fileDir.isDirectory())
                {
                    // 如果是文件,删除文件,创建文件夹
                    fileDir.delete();
                    isSuccess = fileDir.mkdir();
                }
            }
            else
            {
                isSuccess = fileDir.mkdir();
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Init make Dir meet exception.", e);
            isSuccess = false;
        }
        return isSuccess;
    }

    /**
     * 获取当前应用的根目录
     * 
     * @return
     */
    public String getAppFileRoot()
    {
        String appPath = getInnerStoragePath() + File.separator + WORK_DIR
                + File.separator;
        return appPath;
    }

    /**
     * 获取视频目录
     * 
     * @return
     */
    public String getVideoPathDir()
    {
        return getAppFileRoot() + VIDEO_DIR + File.separator;
    }

    public String getVideoRealPath()
    {
        return getAppFileRoot() + VIDEO_DIR + File.separator+"kone.mp4";
    }

    /**
     * 获取视频目录
     *
     * @return
     */
    public String getAudioPathDir()
    {
        return getAppFileRoot() + AUDIO_DIR + File.separator;
    }
    /**
     * 获取图片目录
     * 
     * @return
     */
    public String getImagePathDir()
    {
        return getAppFileRoot() + IMAGE_DIR + File.separator;
    }

    /**
     * 获取默认的图片路径
     * 
     * @return
     */
    public String getDefImagePath()
    {
        String defImagePath = getImagePathDir() + "default.jpg";
        return defImagePath;
    }

    /**
     * 获取布局目录
     * 
     * @return
     */
    public String getLayoutDir()
    {
        String viewPath = getAppFileRoot() + LAYOUT_DIR + File.separator;
        return viewPath;
    }

    /**
     * 获取自定义的视图文件
     *
     * @return 自定义视图路径
     */
    public String getDefaultLayoutDir()
    {
        String viewPath = getLayoutDir() + LAYOUT_DEFAULT_DIR + File.separator;
        return viewPath;
    }

    /**
     * 获取布局的配置文件
     *
     * @return
     */
    public String getCustomViewConfigPath()
    {
        String configPath = getLayoutDir() + CUSTOM_VIEW_CONFIG;
        return configPath;
    }

    /**
     * 播放列表的路径
     * 
     * @return 自定义播放列表
     */
    public String getPlayListPath()
    {
        String playListPath = getAppFileRoot() + PLAY_LIST_NAME;
        return playListPath;
    }

    /**
     * 获取自定义字体存放目录
     * 
     * @return
     */
    public String getCusFontDir()
    {
        String fontDir = getAppFileRoot() + FONT_DIR + File.separator;
        return fontDir;
    }

    /**
     * 获取电梯运行方向图标的目录
     * 
     * @return
     */
    public String getLiftIconDir()
    {
        String liftIconDir = getAppFileRoot() + LIFT_DIR + File.separator;
        return liftIconDir;
    }

    /**
     * 获取电梯图标的配置文件
     * 
     * @return
     */
    public String getLiftIconConfigFile()
    {
        String iconFile = getLiftIconDir() + LIFT_DRAWABLE_CONFIG;
        return iconFile;
    }

    /**
     * 获取字体的配置文件
     * 
     * @return
     */
    public String getFontConfigFile()
    {
        String iconFile = getCusFontDir() + CUSTOM_FONT_CONFIG;
        return iconFile;
    }

    /**
     * 获取电梯配置文件
     * 
     * @return
     */
    public String getElevatorConfigFile()
    {
        String configFile = getAppFileRoot() + ELEVATOR_CONFIG;
        return configFile;
    }
}
