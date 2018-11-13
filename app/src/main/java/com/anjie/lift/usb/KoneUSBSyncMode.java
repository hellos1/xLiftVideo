package com.anjie.lift.usb;

import android.text.TextUtils;

import com.anjie.common.log.LogX;
import com.anjie.common.storage.FileCacheService;
import com.anjie.lift.app.BroadcastCenter;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.config.ConfigManager;
import com.anjie.lift.config.SavePowerMode;
import com.anjie.lift.manager.ControlCenter;
import com.anjie.lift.manager.MPlayerManager;
import com.anjie.lift.parse.ConfigParser;
import com.anjie.lift.usb.info.HiddenArea;
import com.anjie.lift.usb.info.KoneMediaInfo;
import com.anjie.lift.usb.info.KoneUSBSyncInfo;
import com.anjie.lift.usb.info.USBSyncMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KoneUSBSyncMode extends USBSyncMode
{
    /**
     * 日志标签
     */
    private static final String TAG = "KoneUSBSyncMode";

    /**
     * Kone配置文件
     */
    private static final String configFile = "mediascreen.xml";

    /**
     * USB的二级目录update
     */
    private static final String usb_sec_dir = "update" + File.separator;

    /**
     * 是否更新页面的
     */
    private boolean isUpdateView;

    /**
     * 是否有USB同步的配置文件
     * 
     * @param mediaPath
     * @return
     */
    @Override
    public boolean isHasUSBSyncConfig(String mediaPath)
    {
        // Kone的USB同步目录是在USB根目录下面的/update/文件夹下
        return isFileExist(mediaPath + usb_sec_dir, configFile);
    }

    /**
     * 开始同步USB设备
     * 
     * @param usbMediaPath
     * @return
     */
    @Override
    public boolean doSyncUSBDevice(String usbMediaPath)
    {
        // 重置变量
        isUpdateView = false;

        String configFilePath = usbMediaPath + usb_sec_dir + configFile;
        LogX.d(TAG, "configFilePath:" + configFilePath);
        KoneUSBSyncInfo koneUSBSyncInfo = ConfigParser.parseKoneUSBSyncInfo(configFilePath);

        if (koneUSBSyncInfo == null)
        {
            LogX.w(TAG, "Parse KoneUSBSyncInfo is null.");
            return false;
        }
        else
        {
            LogX.d(TAG, "koneUSBSyncInfo:" + koneUSBSyncInfo);
        }

        // 2.优先确认是否要恢复出厂模式
        if (koneUSBSyncInfo.isResetMode())
        {
            // 执行恢复出厂设置
            ControlCenter.getInstance().resetFactoryMode();

            //02018.4.03
            new BroadcastCenter().notifyViewChangeLocal();
            //下面2行 2018.03.29
            MPlayerManager.getInstance().setMediaVolume(ConfigManager.getInstance().getVolume());
            ControlCenter.getInstance().setBrightness(ConfigManager.getInstance().getBrightness());
            return true;
        }

        // 3.检测是否是全屏模式
        checkFullScreenMode(koneUSBSyncInfo.getFullScreenValue());

        // 4.检查标题,滚动字幕和时间显示区域
        checkContent(koneUSBSyncInfo);

        // 5.检查节能模式
        checkSavePowerMode(koneUSBSyncInfo.getStageFirst(), koneUSBSyncInfo.getStageSecond());

        // 6.检查更新播放列表
        checkPlayResource(usbMediaPath + "update/multimedia/", koneUSBSyncInfo.getKoneMediaInfoList());

        // 7.检查亮度
        checkBrightness(koneUSBSyncInfo.getBrightness());

        // 8.检查声音设置
        checkVolume(koneUSBSyncInfo.getVolume());

        // 9.检查时间日期格式
        checkDateFormat(koneUSBSyncInfo.getDateFormat());
        checkTimerFormat(koneUSBSyncInfo.getTimeFormat());

        //10.设置图片显示时间 2018.03.23
        checkImageInterval(koneUSBSyncInfo);

        if (isUpdateView)
        {
            // 重置变量
            isUpdateView = false;
            new BroadcastCenter().notifyViewChangeLocal();
            //休眠模式
            MPlayerManager.getInstance().bootPlay.getAndSet(true);
            //MPlayerManager.getInstance().setTagPTrue();
        }
        return true;
    }

    /**
     * 更新页面
     */
    private void updateViewLater()
    {
        isUpdateView = true;
    }

    /**
     * 检查时间格式
     *
     * @param timeFormat
     */
    private void checkTimerFormat(String timeFormat)
    {
        if ("12".equalsIgnoreCase(timeFormat))
        {
            ConfigManager.getInstance().setTimeFormat("hh:mm");
            updateViewLater();
        }
        else if ("24".equalsIgnoreCase(timeFormat))
        {
            ConfigManager.getInstance().setTimeFormat("kk:mm");
            updateViewLater();
        }
    }


    /**
     * 设置图片显示时间
     */
    //2018.03
    private void checkImageInterval(KoneUSBSyncInfo info){
        if (info != null){
            ConfigManager.getInstance().setImageInterval(info.getPictureInterval());
        }
    }


    /**
     * 检查日期格式
     *
     * @param dateFormat
     */
    private void checkDateFormat(String dateFormat)
    {
        if(!TextUtils.isEmpty(dateFormat) && dateFormat!= null)
        {
            // 兼容格式
            String androidFormat = dateFormat.replace("mm","MM");
            ConfigManager.getInstance().setDateFormat(androidFormat);
        }else {

        }
    }

    /**
     * 验证全屏模式
     */
    private void checkFullScreenMode(int fullScreenValue)
    {
        if(fullScreenValue == 1)
        {
            ConfigManager.getInstance().setFullScreenMode(true);
            updateViewLater();
        }
        else if(fullScreenValue == 0)
        {
            ConfigManager.getInstance().setFullScreenMode(false);
            updateViewLater();
        }
    }

    /**
     * 设置亮度
     * 
     * @param brightness
     */
    private void checkBrightness(int brightness)
    {
        if (brightness > 0)
        {
            // 设置配置
            ConfigManager.getInstance().setBrightness(brightness);
            // 设置系统
            ControlCenter.getInstance().setBrightness(brightness);
        }
    }

    /**
     * 检查声音设置
     * 
     * @param volume
     */
    private void checkVolume(int volume)
    {
        if (volume >= 0)
        {
            // 设置配置记录
            ConfigManager.getInstance().setVolume(volume);
            // 设置播放音量
            MPlayerManager.getInstance().setMediaVolume(volume);
        }
    }

    /**
     * 检查通力播放资源
     * 
     * @param usbMediaPath
     *            USB的media文件夹路径
     * @param list
     *            播放资源列表
     */
    private void checkPlayResource(String usbMediaPath, List<KoneMediaInfo> list)
    {
        if (list != null && list.size() > 0)
        {
            List<String> playItemPathList = new ArrayList<>();
            String playItemPath = null;
            for (KoneMediaInfo info : list)
            {
                playItemPath = doCopy(usbMediaPath, info.getMediaPath(), info.getType());
                if (!TextUtils.isEmpty(playItemPath))
                {
                    playItemPathList.add(playItemPath);
                }

                // 构建播放列表
                if (playItemPathList.size() > 0)
                {
                    String playListContent = buildPlayItem(playItemPathList);
                    if (!TextUtils.isEmpty(playListContent))
                    {
                        String playListPath = FileManager.getInstance().getPlayListPath();
                        boolean bWriteFile = FileCacheService.writeFile(playListPath, playListContent);
                        LogX.i(TAG,"Sync play resource size:"+ playItemPathList.size()
                                        + ",writeFileResult:" + bWriteFile);
                        if (bWriteFile)
                        {
                            new BroadcastCenter().notifyPlaylistChange();
                        }
                    }
                }
            }
        }
        else
        {
            LogX.i(TAG, "No play resource to sync.");
        }
    }

    /**
     * 检查节能模式
     * 
     * @param first
     * @param second
     */
    private void checkSavePowerMode(SavePowerMode first, SavePowerMode second)
    {
        if (first != null)
        {
            ConfigManager.getInstance().setSavePowerMode(first, 1);
        }

        if (second != null)
        {
            ConfigManager.getInstance().setSavePowerMode(second, 2);
        }
    }

    /**
     * 检查内容
     *
     * @param info KoneUSBSyncInfo
     * @return 是否需要刷新页面
     */
    public void checkContent(KoneUSBSyncInfo info)
    {
        if (info == null)
        {
            return;
        }
        if (info.getTitle() != null)
        {
            ConfigManager.getInstance().setTitle(info.getTitle());
            ControlCenter.getInstance().notifyTitleChange(info.getTitle());
        }
        if (info.getScrollText() != null)
        {
            ConfigManager.getInstance().setScrollText(info.getScrollText());
            ControlCenter.getInstance().notifyScrollTextChange(info.getScrollText());
        }
        List<HiddenArea> list = info.getHiddenAreaList();

        // 根据隐藏的内容区域
        if (list != null && list.size() > 0)
        {
            for (HiddenArea area : list)
            {
                if (area.getAreaType() == HiddenArea.AreaType.ScrollText)
                {
                    boolean is_hiden = area.isHidden();
                    ConfigManager.getInstance().hiddenScrollText(is_hiden);
                    updateViewLater();
                }
                else if (area.getAreaType() == HiddenArea.AreaType.Timer)
                {
                    boolean is_hiden = area.isHidden();
                    ConfigManager.getInstance().hiddenDate(is_hiden);
                    updateViewLater();
                }
                else if (area.getAreaType() == HiddenArea.AreaType.Title)
                {
                    boolean is_hiden = area.isHidden();
                    ConfigManager.getInstance().hiddenTitle(is_hiden);
                    updateViewLater();
                }
            }
        }
    }

    /**
     * 执行拷贝任务
     * 
     * @param usbMediaPath
     * @param subPath
     * @param type
     * @return
     */
    private String doCopy(String usbMediaPath, String subPath, KoneMediaInfo.MediaType type)
    {
        // 获取拷贝的文件名
        String fileName = getFileName(subPath);
        if (TextUtils.isEmpty(fileName))
        {
            return null;
        }
        // 根据规范 TODO
        String srcFilePath = usbMediaPath + subPath;
        String disFilePath = null;
        String playItemPath = null;
        if (type == KoneMediaInfo.MediaType.Picture)
        {
            disFilePath = FileManager.getInstance().getImagePathDir() + fileName;
            playItemPath = FileManager.IMAGE_DIR + File.separator + fileName;
        }
        else if (type == KoneMediaInfo.MediaType.Video)
        {
            disFilePath = FileManager.getInstance().getVideoPathDir() + fileName;
            playItemPath = FileManager.VIDEO_DIR + File.separator + fileName;
        }
        else if (type == KoneMediaInfo.MediaType.Backgroud)
        {
            disFilePath = FileManager.getInstance().getAudioPathDir() + fileName;
            playItemPath = FileManager.AUDIO_DIR + File.separator + fileName;
        }
        boolean bResult = false;
        if (!TextUtils.isEmpty(disFilePath))
        {
            bResult = FileCacheService.copyFile(srcFilePath, disFilePath);
        }
        // 成功就返回构建的播放路径
        return bResult ? playItemPath : null;
    }

    /**
     * 获取文件名
     * 
     * @param path
     * @return
     */
    private String getFileName(String path)
    {
        if (TextUtils.isEmpty(path))
        {
            return null;
        }
        int lastIndex = path.lastIndexOf("/");
        if (lastIndex + 1 >= path.length())
        {
            // 严格校验
            return null;
        }
        return path.substring(lastIndex + 1);
    }
}
