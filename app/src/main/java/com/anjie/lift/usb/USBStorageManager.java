package com.anjie.lift.usb;

import android.text.TextUtils;

import com.anjie.common.log.LogX;
import com.anjie.common.threadpool.Job;
import com.anjie.common.threadpool.SingleTaskExecutor;
import com.anjie.lift.app.BroadcastCenter;
import com.anjie.lift.manager.MPlayerManager;
import com.anjie.lift.usb.info.USBSyncMode;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 拷贝USB存储器的任务
 */
public class USBStorageManager
{
    /**
     * 日志标签
     */
    private static final String TAG = "USBStorage";

    /**
     * 单实例
     */
    private static final USBStorageManager instance = new USBStorageManager();

    /**
     * USB根目录下的文件
     */
    private static String USB_ROOT_MEDIA_DIR = File.separator + "multimedia"
            + File.separator;

    /**
     * 是否正在执行拷贝任务
     */
    private AtomicBoolean isExecuteCopyTask = new AtomicBoolean(false);

    /**
     * 私有构造
     */
    private USBStorageManager()
    {

    }

    /**
     * 获取单实例
     * 
     * @return
     */
    public static USBStorageManager getInstance()
    {
        return instance;
    }

    /**
     * 移除USB停止
     */
    public void removeUSBStorage()
    {

    }

    private String getUSBRootPath(String usbFilePath)
    {
        // 空值校验
        if (TextUtils.isEmpty(usbFilePath))
        {
            LogX.e(TAG, "getUSBMediaPath USB file path is empty.");
            return null;
        }
        // USB挂载路径返回时 file:///mnt/usbhost1/
        if (usbFilePath.length() < 7 || !usbFilePath.startsWith("file://"))
        {
            LogX.e(TAG, "getUSBMediaPath length < 7 or not start file://");
            return null;
        }
        String rootUSBDir = usbFilePath.substring("file://".length())
                + File.separator;
        return rootUSBDir;
    }

    /**
     * 获取USB设备Media文件夹的目录
     * 
     * @param usbFilePath
     * @return
     */
    private String getUSBMediaPath(String usbFilePath)
    {
        // 空值校验
        if (TextUtils.isEmpty(usbFilePath))
        {
            LogX.e(TAG,"getUSBMediaPath USB file path is empty.");
            return null;
        }
        // USB挂载路径返回时 file:///mnt/usbhost1/
        if (usbFilePath.length() < 7 || !usbFilePath.startsWith("file://"))
        {
            LogX.e(TAG,"getUSBMediaPath length < 7 or not start file://");
            return null;
        }
        String rootUSBDir = usbFilePath.substring("file://".length());
        File rootFilePath = new File(rootUSBDir);
        if (!rootFilePath.exists())
        {
            LogX.e(TAG,"getUSBMediaPath not exist.");
            return null;
        }

        String[] usbSecFile = rootFilePath.list();
        if (usbSecFile == null )//|| usbSecFile.length != 1),Liuchun 多几个隐藏文件夹，需额外处理
        {
            return null;
        }
        String realFile="";
        for (int i = 0; i < usbSecFile.length; i++)
        {
            if (usbSecFile[i].contains("LOST") || usbSecFile[i].contains("System"))
            {
                continue;

            }
            else
            {
                realFile = usbSecFile[i];
            }
        }
        // 以上代码都是为了获取USB的根目录;实际的根目录时/mnt/usbhost1/8_4/
        // 8_4盘符估计因系统差异
        // 实际的目录/mnt/usbhost1/8_4/media/
        final String srcFileDir = rootUSBDir + File.separator + realFile//usbSecFile[0]
                + USB_ROOT_MEDIA_DIR;
        return srcFileDir;
    }

    /**
     * 挂载USB存储设备
     */
    public void scanUSBStorage(String usbFilePath)
    {
        // 获取USB的路径，这个要适配,不同板子可能有差异
        final String usbMediaPath = getUSBMediaPath(usbFilePath);
        final String usbRootPath = getUSBRootPath(usbFilePath);
        if (TextUtils.isEmpty(usbMediaPath))
        {
            LogX.i(TAG, "scanUSBStorage usbMediaPath is empty."+usbMediaPath);
            return;
        }
        if (isExecuteCopyTask.getAndSet(true))
        {
            // 如果已经在执行扫描任务,则返回
            LogX.i(TAG, "isExecuteCopyTask return.");
            return;
        }

        // 停止播放器
        MPlayerManager.getInstance().stopPlayTask(true);
        MPlayerManager.getInstance().has_set_display.set(false);//2018.06.13
        final BroadcastCenter bCenter = new BroadcastCenter();
        bCenter.notifyUSBSyncDataBegin();
        LogX.i(TAG, "Begin to execute thread");
        // 使用线程执行
        SingleTaskExecutor.getInstance().submit(new Job<Boolean>()
        {
            @Override
            public Boolean run()
            {
                // 1.先处理KONE的USB同步检测,USB的media文件夹下,mediascreen.xml
                USBSyncMode koneUSBSyncMode = new KoneUSBSyncMode();
                LogX.i(TAG, "config path is: " + usbRootPath);
                if (koneUSBSyncMode.isHasUSBSyncConfig(usbRootPath))
                {
                    LogX.i(TAG, "KoneUSBSync begin.");
                    boolean bResult = koneUSBSyncMode.doSyncUSBDevice(usbRootPath);
                    LogX.i(TAG, "KoneUSBSync end.bResult:" + bResult);
                }
                // 2.处理Anjie的USB同步检测任务,USB的media文件夹下AnjieSync.xml
                USBSyncMode anjieUSBSyncMode = new AnjieUSBSyncMode();
                if (anjieUSBSyncMode.isHasUSBSyncConfig(usbRootPath))
                {
                    LogX.i(TAG, "AnjieUSBSync begin.");
                    boolean bResult = koneUSBSyncMode.doSyncUSBDevice(usbMediaPath);
                    LogX.i(TAG, "AnjieUSBSync end.bResult:" + bResult);
                }
                // USB同步任务完成
                isExecuteCopyTask.set(false);
                bCenter.notifyUSBSyncDataFinish();
                return true;
            }
        });
    }
}
