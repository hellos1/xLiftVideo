package com.anjie.lift.usb;

import android.graphics.Typeface;
import android.text.TextUtils;

import com.anjie.common.log.LogX;
import com.anjie.common.storage.FileCacheService;
import com.anjie.lift.app.AppInfoManager;
import com.anjie.lift.app.BroadcastCenter;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.config.USBSyncInfo;
import com.anjie.lift.parse.ConfigParser;
import com.anjie.lift.parse.ViewParser;
import com.anjie.lift.usb.info.USBSyncMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 安杰USB同步模块
 */
public class AnjieUSBSyncMode extends USBSyncMode
{
    /**
     * 日志标签
     */
    private static final String TAG = "AnjieUSBSyncMode";

    /**
     * USB根目录下的视频文件路径video
     */
    private static String USB_MEDIA_VIDEO_DIR = FileManager.VIDEO_DIR
            + File.separator;

    /**
     * USB根目录下的图片文件路径image
     */
    private static String USB_MEDIA_IMAGE_DIR = FileManager.IMAGE_DIR
            + File.separator;

    /**
     * 同步的配置文件
     */
    private static final String configFile = "AnjieSync.xml";

    /**
     * 安杰USB同步模式
     */
    public AnjieUSBSyncMode()
    {

    }

    /**
     * 同步字体
     *
     * @param usbMediaPath
     * @param fontFileName
     * @return
     */
    private boolean doSyncFont(String usbMediaPath, String fontFileName)
    {
        boolean bResult = false;
        if (TextUtils.isEmpty(fontFileName))
        {
            return bResult;
        }
        String usbFullPath = usbMediaPath + fontFileName;
        Typeface typeFace;
        try
        {
            typeFace = Typeface.createFromFile(usbFullPath);
        }
        catch (Exception ex)
        {
            // 加载失败会抛出RuntimeException异常
            typeFace = null;
            LogX.e(TAG, "sync font file " + fontFileName + " meet exception.",
                    ex);
        }
        if (typeFace != null)
        {
            // 先删除APP目录下的字体文件夹
            String appFontDir = FileManager.getInstance().getCusFontDir();
            File f = new File(appFontDir);
            if (f.exists())
            {
                f.delete();
            }
            f.mkdir();
            String appFullPath = FileManager.getInstance().getCusFontDir()
                    + fontFileName;
            boolean bCopyFile = FileCacheService.copyFile(usbFullPath,
                    appFullPath);
            boolean bWriteFile = ConfigParser.writeFontConfigFile(fontFileName);
            boolean reload = AppInfoManager.getInstance().initTypeFace();
            LogX.d(TAG, "syncFont copy file:" + bCopyFile
                    + ",write config file:" + bWriteFile + ",reload:" + reload);
            if (bCopyFile && bWriteFile && reload)
            {
                bResult = true;
            }
        }
        return bResult;
    }

    /**
     * 执行同步电梯图标
     * 
     * @param usbMediaPath
     * @param syncInfo
     */
    private void doSyncLiftInfo(String usbMediaPath, USBSyncInfo syncInfo)
    {
        String upFileName = syncInfo.getLiftUpFileName();
        String downFileName = syncInfo.getLiftDownFileName();
        String arriveFileName = syncInfo.getLiftArriveFileName();
        if (TextUtils.isEmpty(upFileName) || TextUtils.isEmpty(downFileName)
                || TextUtils.isEmpty(arriveFileName))
        {
            // 必须三个文件一起同步
            return;
        }
        File upIconFile = new File(usbMediaPath, upFileName);
        File downIconFile = new File(usbMediaPath, downFileName);
        File arrIconFile = new File(usbMediaPath, arriveFileName);
        if (!upIconFile.exists() || !downIconFile.exists()
                || !arrIconFile.exists())
        {
            // 三个文件必须存在
            return;
        }
        // 先删除lift文件夹下所有内容
        String liftDir = FileManager.getInstance().getLiftIconDir();
        File file = new File(liftDir);
        if (file.exists())
        {
            file.delete();
        }
        file.mkdir();
        String disUpFile = FileManager.getInstance().getLiftIconDir()
                + upFileName;
        String disDownFile = FileManager.getInstance().getLiftIconDir()
                + downFileName;
        String disArriveFile = FileManager.getInstance().getLiftIconDir()
                + arriveFileName;
        FileCacheService.copyFile(upIconFile, disUpFile);
        FileCacheService.copyFile(downIconFile, disDownFile);
        FileCacheService.copyFile(arrIconFile, disArriveFile);
        ConfigParser.wirteLiftIconConfigFile(upFileName, downFileName,
                arriveFileName);
        AppInfoManager.getInstance().initDirectionDrawable();
    }

    /**
     * 同步自定义的View布局文件
     *
     * @param usbMediaPath
     * @param viewFileName
     * @return
     */
    private boolean doSyncViewFile(String usbMediaPath, String viewFileName)
    {
        if (TextUtils.isEmpty(viewFileName))
        {
            return false;
        }
        if (!viewFileName.toLowerCase().endsWith(".zip"))
        {
            // 必须是zip格式的文件
            return false;
        }
        // 操作结果
        boolean bResult = false;
        String usbFullPath = usbMediaPath + viewFileName;
        // 布局文件(ZIP格式文件)的绝对路径
        String appCustomViewFilePath = FileManager.getInstance().getLayoutDir() + viewFileName;
        // 拷贝布局文件到(手机内部存储/elevator/layout/)目录下
        boolean isCopySuc = FileCacheService.copyFile(usbFullPath,
                appCustomViewFilePath);
        if (isCopySuc)
        {
            // 获取布局文件名(hello.zip)获取后的就是hello,".zip"长度4
            String fileDir = viewFileName.substring(0,
                    viewFileName.length() - 4);
            String appCustomViewDir = FileManager.getInstance().getLayoutDir() + fileDir;
            // 创建目录
            if (FileManager.makeDir(appCustomViewDir))
            {
                // 解压布局文件到目录
                boolean isUnzipSuc = FileCacheService
                        .unzip(appCustomViewFilePath, appCustomViewDir);
                if (isUnzipSuc)
                {
                    // 生成布局的配置文件
                    String configContent = ViewParser
                            .buildLayoutConfigContent(fileDir);
                    bResult = FileCacheService.writeFile(
                            FileManager.getInstance().getCustomViewConfigPath(),
                            configContent);
                }
            }
        }
        return bResult;
    }

    /**
     * 同步升级APK的文件
     *
     * @param usbMediaPath
     * @param apkFileName
     * @return
     */
    private boolean doSyncLiftApk(String usbMediaPath, String apkFileName)
    {
        if (TextUtils.isEmpty(apkFileName) || !apkFileName
                .toLowerCase(Locale.getDefault()).endsWith(".apk"))
        {
            return false;
        }
        String usbFullPath = usbMediaPath + apkFileName;
        String apkFullPath = FileManager.getInstance().getAppFileRoot()
                + FileManager.APK_NAME;
        boolean bResult = FileCacheService.copyFile(usbFullPath, apkFullPath);
        return bResult;
    }

    @Override
    public boolean isHasUSBSyncConfig(String mediaPath)
    {
        // 验证安杰的USB同步配置文件
        return isFileExist(mediaPath, configFile);
    }

    @Override
    public boolean doSyncUSBDevice(String usbMediaPath)
    {
        // 1. 先读取USB存储器中的配置文件
        USBSyncInfo syncInfo = ConfigParser
                .parserUSBStorage(usbMediaPath + configFile);
        LogX.d(TAG, "Parse USBSyncInfo:" + syncInfo);

        // 2. 同步多媒体播放资源,生成新的播放列表playlist.xml
        boolean isPlayListUpdate = doSyncMedia(usbMediaPath,
                syncInfo.getMediaSyncType());
        // 3.同步是否有APK要升级
        boolean isApkUpdate = doSyncLiftApk(usbMediaPath,
                syncInfo.getApkFileName());
        // 4.同步是否有布局要更新
        boolean isViewUpdate = doSyncViewFile(usbMediaPath,
                syncInfo.getViewFileName());
        // 5.同步是否有字体库需要更新
        boolean isFontUpdate = doSyncFont(usbMediaPath,
                syncInfo.getFontFileName());
        // 6.同步是否有电梯运行图标更新
        doSyncLiftInfo(usbMediaPath, syncInfo);
        BroadcastCenter bCenter = new BroadcastCenter();

        if (isViewUpdate || isFontUpdate)
        {
            // 有自定义布局,字体更新成功也需要重新加载布局,重新设置控件字体格式
            bCenter.notifyViewChange();
        }
        if (isPlayListUpdate)
        {
            // 发送广播通知播放列表加载
            // 广播接收到之后重新加载播放列表的时候会对比新旧播放列表,删除旧的资源，检查那段代码是否取消注释掉
            bCenter.notifyPlaylistChange();
        }

        if (isApkUpdate)
        {
            // APK有更新,则更新安装应用
            doAPKUpdate();
        }
        return true;
    }

    /**
     * 开始同步多媒体信息
     * 
     * @param usbMediaPath
     * @param syncType
     * @return 是否生成了新的播放列表
     */
    private boolean doSyncMedia(String usbMediaPath, int syncType)
    {
        List<String> allVideoFNameList = new ArrayList<String>();
        List<String> allImageFNameList = new ArrayList<String>();
        List<String> tempList = null;
        if (syncType == 1)
        {
            // 增量同步,先保存所有video和image下的文件
            allImageFNameList.addAll(getAllFileName(
                    FileManager.getInstance().getImagePathDir()));
            allVideoFNameList.addAll(getAllFileName(
                    FileManager.getInstance().getVideoPathDir()));
        }
        // 拷贝文件(USB设备下video/image下的文件)
        allImageFNameList.addAll(doCopy(usbMediaPath, USB_MEDIA_IMAGE_DIR));
        allVideoFNameList.addAll(doCopy(usbMediaPath, USB_MEDIA_VIDEO_DIR));

        // 建立播放列表项
        List<String> allMediaPlayPathList = new ArrayList<String>();
        for (String fileName : allImageFNameList)
        {
            allMediaPlayPathList.add(USB_MEDIA_IMAGE_DIR + fileName);
        }
        for (String fileName : allVideoFNameList)
        {
            allMediaPlayPathList.add(USB_MEDIA_VIDEO_DIR + fileName);
        }
        // 构建播放列表内容
        String playListContent = buildPlayItem(allMediaPlayPathList);
        if (!TextUtils.isEmpty(playListContent))
        {
            String playListFile = FileManager.getInstance().getPlayListPath();
            return FileCacheService.writeFile(playListFile, playListContent);
        }
        return false;
    }

    /**
     * 拷贝文件
     * 
     * @param usbMediaPath
     * @param mediaDir
     * @return
     */
    private List<String> doCopy(String usbMediaPath, String mediaDir)
    {
        List<String> mediaFileNameList = new ArrayList<String>();
        List<String> usbMediaFileList = getAllFileName(usbMediaPath + mediaDir);
        if (usbMediaFileList != null && usbMediaFileList.size() > 0)
        {
            String srcFilePath = null;
            String disFilePath = null;
            for (String fileName : mediaFileNameList)
            {
                srcFilePath = usbMediaPath + mediaDir + fileName;
                if (USB_MEDIA_VIDEO_DIR.equals(mediaDir))
                {
                    disFilePath = FileManager.getInstance().getVideoPathDir()
                            + fileName;
                }
                else if (USB_MEDIA_IMAGE_DIR.equals(mediaDir))
                {
                    disFilePath = FileManager.getInstance().getImagePathDir()
                            + fileName;
                }
                else
                {
                    continue;
                }
                if (FileCacheService.copyFile(srcFilePath, disFilePath))
                {
                    // 拷贝成功添加到列表
                    mediaFileNameList.add(mediaDir + fileName);
                }
            }
        }
        return mediaFileNameList;
    }

    /**
     * 获取所有指定类型的文件名
     *
     * @param fileDirPath
     * @return
     */
    private List<String> getAllFileName(String fileDirPath)
    {
        List<String> fileNameList = new ArrayList<String>();
        File fileDir = new File(fileDirPath);
        File[] allFiles = fileDir.listFiles();
        if (allFiles != null && allFiles.length > 0)
        {
            String fileName;
            for (File file : allFiles)
            {
                if (file == null)
                {
                    continue;
                }
                fileName = file.getName();
                if (!TextUtils.isEmpty(fileName)
                        && !fileName.endsWith(".download")
                        && !fileName.equalsIgnoreCase("default.jpg"))
                {
                    // 过滤掉正在下载的文件,和默认的播放图片
                    fileNameList.add(fileName);
                }
            }
        }
        return fileNameList;
    }

    /**
     * 执行APK升级过程代码
     */
    private void doAPKUpdate()
    {
        // 执行升级的代码 TODO
    }
}
